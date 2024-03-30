package com.example.jarvis.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

public class SoftKeyboardStateHelper implements ViewTreeObserver.OnGlobalLayoutListener {

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<>();
    private final View activityRootView;
    private int lastSoftKeyboardHeightInPx;
    private boolean isSoftKeyboardOpened;

    private final int[] temp = new int[2];

    public SoftKeyboardStateHelper(View activityRootView) {
        this(activityRootView, false);
    }

    public SoftKeyboardStateHelper(View activityRootView, boolean isSoftKeyboardOpened) {
        this.activityRootView = activityRootView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        activityRootView.getWindowVisibleDisplayFrame(r);

//        final int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
//        if (!isSoftKeyboardOpened && heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
//            isSoftKeyboardOpened = true;
//            notifyOnSoftKeyboardOpened(heightDiff);
//        } else if (isSoftKeyboardOpened && heightDiff < 100) {
//            isSoftKeyboardOpened = false;
//            notifyOnSoftKeyboardClosed();
//        }

        /*
         * 是这样的，由于某些特殊情况，我需要判断软键盘是否弹出或收起。
         * 而这个代码是我从网上找到的。
         * 它利用当前 activity 的根视图（activityRootView）在屏幕上的可见部分（即未被系统栏如状态栏或导航栏遮挡的部分）的尺寸变化来判断界面状态。
         * 当然，软键盘也是会遮挡根视图的可见部分的。
         * 所以在发现 r.height()（r 是可见部分，r.height() 就是可见部分的高度）变小的时候，我们有理由怀疑是软键盘弹出了。
         * 同理可证，当 r.height()变大的时候，就是软键盘收起了。
         * 而上面注释掉的代码就是网上原本写好的判断 r.height()变化的代码，但是在我的系统中并不适用，所以我就重写了这部分的内容。
         * 在通过不断的测试 r.height()的值之后，我发现，不管是在虚拟机还是我的手机中，r.height()的值不管怎么输出，都只会在两个数之间反复横跳。
         * 而这两个数虽然在不同的机器上面会有所不同，但永远都是一个大一个小。
         * 再通过我刚才的合理怀疑，不难发现，当 r.height()获取到大值到时候，软键盘收起，反之取到小值，软键盘打开。
         * 所以下面的代码应运而生。
         * 简单说一下就是，首先获取可见部分的高度 heightDiff。
         * 其次，由于 heightDiff 会且仅会出现两个值，所以我提前创建了一个长度为 2 的数组 temp（定义的语句在上面），默认存储数据为 0。
         * 获取这两个值的逻辑是这样的，如果 temp[1] 的值为空（为默认值 0）则直接将获取到的 heightDiff 存入，
         * 如果 temp[1] 的值不为空，则判断 heightDiff 的值是否与 temp[1] 中的值相同，若不同再判断 heightDiff 的值是否与 temp[0] 相同，
         * 这里会遇到两种情况，一是 temp[0] 为空（为默认值 0），则 temp[0] 的值与 heightDiff 一定不相同，故将 heightDiff 存入 temp[0] 中
         * 另外一种情况就是 temp[0] 已经存有数值，再通过 heightDiff 只会输出两个一大一小的数的特性，heightDiff 与 temp[0] 的值一定相同，故舍弃。
         * 在获取到两个数之后，就需要对这两个数比大小，确定软键盘开启时的高度 openHeightDiff 和软键盘关闭时的高度 closeHeightDiff。
         * 在这之后就进入到了最重要的地方，判断软键盘状态。
         * 首先我要说明的是输入框的出现与软键盘的弹出肯定不是同步的，我的系统的逻辑是先出现输入框再弹出软键盘，
         * 所以heightDiff首先会输出软键盘收起时的高度 closeHeightDiff，此时就会出现一个问题，
         * 如果简单的以 heightDiff == closeHeightDiff 作为判断软键盘状态的条件的话，就会出现以下描述的情况：
         * 点击手动输入按钮之后，输入框出现又立刻收回
         * 其原因就是，在输入框弹出的瞬间，系统判断软键盘收起，随之立即执行关闭弹窗的指令。
         * 解决这个问题的方法就是给判断条件加一个锁（isSoftKeyboardOpened），
         * 若软键盘弹出则 isSoftKeyboardOpened=true，反之 isSoftKeyboardOpened=false，
         * 同时它是在将这个类实体化时，其构造函数所需要的一个参数，默认为 false。
         * 此时判断软键盘状态的条件就不再是简单的 heightDiff == closeHeightDiff 或 heightDiff == openHeightDiff了，
         * 而是下面代码中的形式（太长了，懒得写）
         * 说人话就是，只有在 isSoftKeyboardOpened=true 且 heightDiff == closeHeightDiff 时系统才会判断软键盘收起，
         * 这样做就避免了，系统在一开始直接判定软键盘收起，同时执行关闭弹窗指令的操作了（因为一开始 isSoftKeyboardOpened 的值为 false）。
         * 同时，isSoftKeyboardOpened 也不会对软键盘的开启判断有影响。
         * 因为虽然一开始 !isSoftKeyboardOpened=true，但是 heightDiff(=closeHeightDiff) 与 openHeightDiff(=0)一定不相同，
         * 所以也不会判断软键盘为开启状态。
         * 之后，只要 heightDiff 输出另一个值（openHeightDiff），本系统就可以开始判断了。
         * 如此，判断软键盘状态的代码就写完了，可喜可贺 可喜可贺。
         * （写这些话的原因就是，我怕到时候我忘了这段代码是干什么的了，所以就想随手记一下，没想到写了这么多）
         */
        final int heightDiff = (r.height()); //可见部分的高度

        if (temp[1] == 0) temp[1] = heightDiff;
        else if (heightDiff != temp[1] && heightDiff != temp[0]) temp[0] = heightDiff;
        int openHeightDiff = Math.min(temp[0], temp[1]); //软键盘开启时的高度
        int closeHeightDiff = Math.max(temp[0], temp[1]); //软键盘关闭时的高度

        //软键盘状态判断
        if (!isSoftKeyboardOpened && heightDiff == openHeightDiff) {
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff);
        } else if (isSoftKeyboardOpened && heightDiff == closeHeightDiff) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    /**
     * Default value is zero (0)
     *
     * @return last saved keyboard height in px
     */
    public int getLastSoftKeyboardHeightInPx() {
        return lastSoftKeyboardHeightInPx;
    }

    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx) {
        this.lastSoftKeyboardHeightInPx = keyboardHeightInPx;
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardOpened(keyboardHeightInPx);
            }
        }
    }

    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
    }

    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened(int keyboardHeightInPx);

        void onSoftKeyboardClosed();
    }
}
