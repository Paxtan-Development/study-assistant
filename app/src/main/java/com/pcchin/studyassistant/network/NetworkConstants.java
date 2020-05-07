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

package com.pcchin.studyassistant.network;

import com.pcchin.studyassistant.BuildConfig;

/** Constants used in network communication. **/
public final class NetworkConstants {
    public static final String MAIN_API = "https://api.paxtan.dev";
    // public static final String MAIN_API = "http://10.0.2.2:3000";
    public static final String BACKUP_API = "https://api.pcchin.com";
    public static final String SEC_BACKUP_API = "https://paxtandev.herokuapp.com";
    @SuppressWarnings("ConstantConditions")
    public static final String UPDATE_PATH = BuildConfig.BUILD_TYPE.equals("beta")
            ? "/study-assistant/beta" : "/study-assistant/latest";
    public static final String FEEDBACK_PATH = "/study-assistant/feedback";
    public static final String BUG_PATH = "/study-assistant/bug";
    /* Example user agent: "Study-Assistant/1.5 (...)" */
    @SuppressWarnings("ConstantConditions")
    public static final String USER_AGENT = System.getProperty("http.agent","")
            .replaceAll("^.+?/\\S+", String.format("Study-Assistant/%s", BuildConfig.VERSION_NAME));
}
