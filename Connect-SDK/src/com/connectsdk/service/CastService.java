/*
 * CastService
 * Connect SDK
 *
 * A deliberately thin DeviceService: its only job is to let DiscoveryManager register
 * CastDiscoveryProvider and surface Google Cast / Chromecast-built-in devices (which is
 * how most Android TV and Google TV boxes advertise themselves) as ConnectableDevices.
 *
 * This app drives the actual Android TV Remote pairing/control protocol itself
 * (see com.sdk.remote.androidremote.android.PairingManager, wired up in
 * ConnectableDevice.connect()'s DeviceType.ANDROID branch), so this class intentionally
 * does not implement Cast session/media capabilities - only enough of the DeviceService
 * contract for discovery to work, matching how FireTVService/RokuService are used as
 * thin discovery markers elsewhere in this module.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.service;

import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

public class CastService extends DeviceService {

    public static final String ID = "Chromecast";

    // Default Media Receiver app id, used only to build the MediaRouteSelector control
    // category that CastDiscoveryProvider scans for.
    private static String applicationId = "CC1AD845";

    public CastService(ServiceDescription serviceDescription, ServiceConfig serviceConfig) {
        super(serviceDescription, serviceConfig);
    }

    /**
     * Get filter instance for this service which contains a name of service and id. It is
     * used in the discovery process.
     */
    public static DiscoveryFilter discoveryFilter() {
        return new DiscoveryFilter(ID, ID);
    }

    public static String getApplicationID() {
        return applicationId;
    }

    public static void setApplicationID(String applicationId) {
        CastService.applicationId = applicationId;
    }

    @Override
    public boolean isConnectable() {
        return false;
    }
}
