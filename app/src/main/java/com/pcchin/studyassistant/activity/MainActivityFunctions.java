/*
 * Copyright 2020 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant.activity;

import android.app.Activity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragment;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectMediaFragment;
import com.pcchin.studyassistant.fragment.project.member.ProjectMemberListFragment;
import com.pcchin.studyassistant.fragment.project.role.ProjectRoleFragment;
import com.pcchin.studyassistant.fragment.project.status.ProjectStatusFragment;
import com.pcchin.studyassistant.fragment.project.task.ProjectTaskFragment;

import java.util.List;

/** Functions that are used in MainActivity. **/
public final class MainActivityFunctions {
    private final MainActivity activity;

    /** Constructor used as activity needs to be passed on. **/
    MainActivityFunctions(MainActivity activity) {
        this.activity = activity;
    }

    /** Checks whether a fragment has a bottom nav view. **/
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean fragmentHasBottomNavView(Fragment fragment) {
        return fragment instanceof ProjectInfoFragment
                || fragment instanceof ProjectMemberListFragment
                || fragment instanceof ProjectTaskFragment
                || fragment instanceof ProjectRoleFragment
                || fragment instanceof ProjectStatusFragment
                || fragment instanceof ProjectMediaFragment;
    }

    /** Gets the pager adapter for the notes. **/
    @NonNull
    FragmentStatePagerAdapter getNoteAdapter(List<NotesContent> notesList) {
        return new NotePagerAdapter(notesList, activity.getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            // getItem does not correspond to the current item selected, DO NOT USE IT AS SUCH
            public Fragment getItem(int position) {
                // This if else is used to prevent ArrayOutOfBoundsException for items outside the range
                if (position < notesList.size() && position >= 0) {
                    return NotesViewFragment.newInstance(notesList.get(position).noteId);
                } else {
                    return new Fragment();
                }
            }

            @Override
            public int getCount() {
                return notesList.size();
            }
        };
    }

    /** A FragmentStatePagerAdapter which contains a notes list, which can then be passed on to
     * @see com.pcchin.studyassistant.ui.NoteViewPager **/
    public abstract static class NotePagerAdapter extends FragmentStatePagerAdapter {
        public final List<NotesContent> notesList;

        public NotePagerAdapter(List<NotesContent> notesList, FragmentManager fm, int behavior) {
            super(fm, behavior);
            this.notesList = notesList;
        }
    }

    /** Returns the page change listener for the note adapter. **/
    @NonNull
    ViewPager.OnPageChangeListener getNoteAdapterPageChanger(FragmentStatePagerAdapter baseAdapter) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not needed
            }

            @Override
            public void onPageSelected(int position) {
                // instantiateItem used instead of getItem as getItem returns a new instance of
                // a fragment instead of an existing one
                activity.currentFragment = (Fragment) baseAdapter.instantiateItem(activity.findViewById(R.id.base), position);
                activity.invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not needed
            }
        };
    }

    /** Fades out to another note to increase smoothness. **/
    void fadeToNote() {
        activity.findViewById(R.id.base).startAnimation(AnimationUtils.loadAnimation(
                activity.getApplicationContext(), R.anim.fadeout));
        if (activity.pager.getAdapter() != null) {
            // Set the current fragment as the first fragment in the pager adapter
            activity.currentFragment = (Fragment) activity.pager.getAdapter()
                    .instantiateItem(activity.findViewById(R.id.base), 0);
        }
        activity.findViewById(R.id.base).setVisibility(View.GONE);
        activity.pager.setVisibility(View.VISIBLE);
        activity.pager.setCurrentItem(0, false);
        hideKeyboard();
    }

    /** Hides the soft input keyboard, separated for clarity. **/
    void hideKeyboard() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
