package io.github.tonyguyot.acronym.fragments;

/**
 * Define callbacks useful for a fragment being an item of a
 * ViewPager.
 */
public interface ViewPagerFragmentLifecycle {

    // will be called when the fragment is displayed in the ViewPager
    void onShowInViewPager();

    // will be called when the fragment is no more displayed in the ViewPager
    void onHideInViewPager();
}
