RxWifiP2pManager
===========================

An RxJava wrapper for Android's [WifiP2pManager](https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html), allowing you to use RxJava to manage Wi-Fi peer-to-peer connectivity. 

>This lets an application discover available peers, setup connection to peers and query for the list of peers. When a p2p connection is formed over wifi, the device continues to maintain the uplink connection over mobile or any other available network for internet connectivity on the device.

*-- https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pManager.html*

## Requirements
Min sdk is API level 14.

## Communication
* Author: Stefan Mitev
* E-mail: mr.mitew [at] gmail . com
* [Github issues](https://github.com/mrmitew/RxWifiP2pManager/issues)

## Some examples
### Instantiation of RxWifiP2pManager
```java
mRxWifiP2pManager = new RxWifiP2pManager(
                        getApplicationContext(),
                        (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE),
                        new WifiP2pBroadcastObservableManagerFactory(getApplicationContext()));
```
### Request current p2p peers
```java
mRxWifiP2pManager.requestPeers()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wifiP2pDevice -> Log.d("log", "Device found: " + wifiP2pDevice.deviceName));
```
### Discover and request all nearby p2p peers
```java
mRxWifiP2pManager.discoverAndRequestPeersList()
                .timeout(5, TimeUnit.SECONDS) // Optional, but recommended
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wifiP2pDevice -> Log.d("log", "Device found: " + wifiP2pDevice.deviceName));
```
### Discover and request a list with all current nearby p2p peers
```java
mRxWifiP2pManager.discoverAndRequestPeersList()
                .timeout(5, TimeUnit.SECONDS) // Optional, but recommended
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wifiP2pDeviceList -> Log.d("log", Arrays.toString(wifiP2pDeviceList.getDeviceList().toArray())));
```
### Initiation of a peer discovery
```java
mRxWifiP2pManager.discoverPeers()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.d("log", "Discover completed"));
```

### Initiation of a connection request to a peer
```java
mRxWifiP2pManager.connect(mRxWifiP2pManager.createConfig("<mac address>", WpsInfo.PBC))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.d("log", "Connected"));
```
### Requesting information about the current connection
```java
mRxWifiP2pManager.requestConnectionInfo()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wifiP2pInfo -> Log.d("log", wifiP2pInfo.toString()));
```
### Remove an existing p2p group
```java
mRxWifiP2pManager.disconnect()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.d("log", "Disconnected"));
```

## To-do
Not all APIs from Android's WifiP2pManager are included into this library, so this is something to do for a future release. PRs are welcome! :)

## Sample app
Demonstrates several of the RxWifiP2pManager's APIs, including:

* discovering nearby p2p devices
* connecting to a device
* requesting/displaying connection information
* disconnect from an existing p2p group

### Screenshots

![Welcome screen](https://github.com/mrmitew/RxWifiP2pManager/blob/master/app/design/welcome.png) ![Discovering devices](https://github.com/mrmitew/RxWifiP2pManager/blob/master/app/design/discovering.png) ![Discovered devices](https://github.com/mrmitew/RxWifiP2pManager/blob/master/app/design/discovered-devices.png)
![Connected to a device](https://github.com/mrmitew/RxWifiP2pManager/blob/master/app/design/connected.png) ![Requested connection information](https://github.com/mrmitew/RxWifiP2pManager/blob/master/app/design/requsted-connection-info.png)
## Dependencies
### Sample app

```groovy
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:25.0.0'
    compile 'com.android.support:design:25.0.0'
    testCompile 'junit:junit:4.12'

    debugCompile 'com.squareup.leakcanary:leakcanary-android:1.4-beta2'
    releaseCompile 'com.squareup.leakcanary:leakcanary-android-no-op:1.4-beta2'
    testCompile 'com.squareup.leakcanary:leakcanary-android-no-op:1.4-beta2'

    compile project(":library")

    // SVG lib
    compile 'com.pixplicity.sharp:library:1.0.2@aar'

    // Butterknife
    compile 'com.jakewharton:butterknife:8.1.0'
    apt 'com.jakewharton:butterknife-compiler:8.1.0'

    // RxJava
    compile "io.reactivex:rxjava:1.2.1"
    compile "io.reactivex:rxandroid:1.2.1"
}
```

### Library
```groovy
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    testCompile 'junit:junit:4.12'

    // RxJava
    compile "io.reactivex:rxjava:1.2.1"

    // RxBroadcast
    compile 'com.cantrowitz:rxbroadcast:1.1.0'
}
```

## Build
```shell
$ git clone https://github.com/mrmitew/RxWifiP2pManager.git
$ cd rxwifip2pmanager/
$ ./gradlew build
```

## Bugs and Feedback
For bugs, questions and discussions please use the Github Issues.

## Changelog
* 1.0.0 - Initial version

## License
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
