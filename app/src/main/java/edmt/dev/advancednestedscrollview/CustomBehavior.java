package edmt.dev.advancednestedscrollview;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class CustomBehavior extends CoordinatorLayout.Behavior<NestedScrollView> {

    public CustomBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean layoutDependsOn(
            CoordinatorLayout parent, NestedScrollView child, View dependency) {
         return dependency.getId() == R.id.toolbar_container;
    }

    @Override
    public boolean onLayoutChild(
            CoordinatorLayout parent, NestedScrollView child, int layoutDirection) {
        // First layout the child as normal.
        parent.onLayoutChild(child, layoutDirection);

        // Center the FAB vertically along the top edge of the card.
        final int fabHalfHeight = child.findViewById(R.id.fab).getHeight() / 2;
        setTopMargin(child.findViewById(R.id.cardview), fabHalfHeight);

        // Give the RecyclerView a maximum height to ensure the card will never
        // overlap the toolbar as it scrolls.
        final int rvMaxHeight =
                child.getHeight()
                        - fabHalfHeight
                        - child.findViewById(R.id.card_title).getHeight()
                        - child.findViewById(R.id.card_subtitle).getHeight();
        final MaxHeightRecyclerView rv = child.findViewById(R.id.card_recyclerview);
        rv.setMaxHeight(rvMaxHeight);

        // Give the card container top padding so that only the top edge of the card
        // initially appears at the bottom of the screen. The total padding will
        // be the distance from the top of the screen to the FAB's top edge.
        final View cardContainer = child.findViewById(R.id.card_container);
        final int toolbarContainerHeight =
                parent.getDependencies(child).get(0).getHeight();
        setPaddingTop(cardContainer, rvMaxHeight - toolbarContainerHeight);

        // Offset the child's height so that its bounds don't overlap the
        // toolbar container.
        ViewCompat.offsetTopAndBottom(child, toolbarContainerHeight);

        // Add the same amount of bottom padding to the RecyclerView so it doesn't
        // display its content underneath the navigation bar.
        setPaddingBottom(rv, toolbarContainerHeight);

        // Return true so that the parent doesn't waste time laying out the
        // child again (any modifications made above will have triggered a second
        // layout pass anyway).
        return true;
    }

    private static void setTopMargin(View v, int topMargin) {
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        if (lp.topMargin != topMargin) {
            lp.topMargin = topMargin;
            v.setLayoutParams(lp);
        }
    }

    private static void setPaddingTop(View v, int top) {
        if (v.getPaddingTop() != top) {
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
        }
    }

    private static void setPaddingBottom(View v, int bottom) {
        if (v.getPaddingBottom() != bottom) {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(
            CoordinatorLayout parent, NestedScrollView child, MotionEvent ev) {
        // Block all touch events that originate within the bounds of our
        // NestedScrollView but do *not* originate within the bounds of its
        // inner CardView and FloatingActionButton.
        return ev.getActionMasked() == MotionEvent.ACTION_DOWN
                && isTouchInChildBounds(parent, child, ev)
                && !isTouchInChildBounds(parent, child.findViewById(R.id.cardview), ev)
                && !isTouchInChildBounds(parent, child.findViewById(R.id.fab), ev);
    }

    private static boolean isTouchInChildBounds(
            ViewGroup parent, View child, MotionEvent ev) {
        return MyViewGroupUtils.isPointInChildBounds(
                parent, child, (int) ev.getX(), (int) ev.getY());
    }
}
