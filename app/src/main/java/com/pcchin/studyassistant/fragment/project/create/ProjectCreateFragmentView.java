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

package com.pcchin.studyassistant.fragment.project.create;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;

/** Functions used in the onCreateView function of
 * @see ProjectCreateFragment **/
final class ProjectCreateFragmentView {
    private ProjectCreateFragment fragment;

    /** Constructor for the class as fragment needs to be passed on. **/
    ProjectCreateFragmentView(ProjectCreateFragment fragment) {
        this.fragment = fragment;
    }

    /** Initializes the "Enable Members" switch, used in onCreateView. **/
    void initEnableMembers(@NonNull View returnView) {
        ((Switch) returnView.findViewById(R.id.p6_enable_members))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    fragment.enableMembers = b;
                    if (b) {
                        // Members are enabled
                        returnView.findViewById(R.id.p6_first_member).setVisibility(View.VISIBLE);
                        if (fragment.enableRoles) {
                            returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.VISIBLE);
                        }
                    } else {
                        disableMembers(returnView);
                    }
                });
    }

    /** Disable the member related LicenseViews and their visibilities. **/
    private void disableMembers(@NonNull View returnView) {
        LinearLayout memberGrp = returnView.findViewById(R.id.p6_first_member);
        memberGrp.setVisibility(View.GONE);
        returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.GONE);

        // Disable all error dialogs
        for (int i = 0; i < memberGrp.getChildCount(); i++) {
            if (memberGrp.getChildAt(i) instanceof TextInputLayout) {
                ((TextInputLayout) memberGrp.getChildAt(i)).setErrorEnabled(false);
            }
        }
    }

    /** Initializes the "Enable Roles" switch, used in onCreateView. **/
    void initEnableRoles(@NonNull View returnView) {
        ((Switch) returnView.findViewById(R.id.p6_enable_roles))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    fragment.enableRoles = b;
                    if (b) {
                        // Roles are enabled
                        returnView.findViewById(R.id.p6_custom_roles).setVisibility(View.VISIBLE);
                        if (fragment.enableMembers) {
                            returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.VISIBLE);
                        }
                    } else {
                        disableRoles(returnView);
                    }
                });
    }

    /** Disable the role related LicenseViews and their visibilities. **/
    private void disableRoles(@NonNull View returnView) {
        LinearLayout roleGrp = returnView.findViewById(R.id.p6_custom_roles);
        roleGrp.setVisibility(View.GONE);
        returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.GONE);

        // Disable all error dialogs in child LinearLayouts
        for (int i = 0; i < roleGrp.getChildCount(); i++) {
            if (roleGrp.getChildAt(i) instanceof LinearLayout) {
                LinearLayout subLayout = (LinearLayout) roleGrp.getChildAt(i);
                for (int j = 0; j < subLayout.getChildCount(); j++) {
                    if (subLayout.getChildAt(j) instanceof TextInputLayout)
                        ((TextInputLayout) subLayout.getChildAt(j)).setErrorEnabled(false);
                }
            }
        }
    }

    /** Initializes the "Custom Admin" switch, used in onCreateView. **/
    void initCustomAdmin(@NonNull View returnView) {
        ((CheckBox) returnView.findViewById(R.id.p6_custom_admin_switch))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    fragment.customAdmin = b;
                    if (b) {
                        // Custom admin is enabled
                        returnView.findViewById(R.id.p6_custom_admin).setVisibility(View.VISIBLE);
                    } else {
                        // Custom admin is disabled
                        LinearLayout adminView = returnView.findViewById(R.id.p6_custom_admin);
                        adminView.setVisibility(View.GONE);

                        // Disable all error dialogs
                        for (int i = 0; i < adminView.getChildCount(); i++) {
                            if (adminView.getChildAt(i) instanceof TextInputLayout) {
                                ((TextInputLayout) adminView.getChildAt(i)).setErrorEnabled(false);
                            }
                        }
                    }
                });
    }

    /** Initializes the "Custom Member" switch, used in onCreateView. **/
    void initCustomMember(@NonNull View returnView) {
        ((CheckBox) returnView.findViewById(R.id.p6_custom_member_switch))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    fragment.customMember = b;
                    if (b) {
                        // Custom members enabled
                        returnView.findViewById(R.id.p6_custom_member).setVisibility(View.VISIBLE);
                    } else {
                        // Custom members disabled
                        LinearLayout memberView = returnView.findViewById(R.id.p6_custom_member);
                        memberView.setVisibility(View.GONE);

                        // Disable all error dialogs
                        for (int i = 0; i < memberView.getChildCount(); i++) {
                            if (memberView.getChildAt(i) instanceof TextInputLayout) {
                                ((TextInputLayout) memberView.getChildAt(i)).setErrorEnabled(false);
                            }
                        }
                    }
                });
    }
}
