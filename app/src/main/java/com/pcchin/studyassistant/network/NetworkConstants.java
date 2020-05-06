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
final class NetworkConstants {
    // public static final String MAIN_API = "https://api.paxtan.dev";
    static final String MAIN_API = "http://10.0.2.2:3000";
    static final String BACKUP_API = "https://api.pcchin.com";
    static final String SEC_BACKUP_API = "https://paxtandev.herokuapp.com";
    @SuppressWarnings("ConstantConditions")
    static final String UPDATE_PATH = BuildConfig.BUILD_TYPE.equals("beta")
            ? "/study-assistant/beta" : "/study-assistant/latest";
    static final String FEEDBACK_PATH = "/study-assistant/feedback";
    /* Example user agent: "Study-Assistant/1.5 (...)" */
    @SuppressWarnings("ConstantConditions")
    static final String USER_AGENT = System.getProperty("http.agent","")
            .replaceAll("^.+?/\\S+", String.format("Study-Assistant/%s", BuildConfig.VERSION_NAME));
}
