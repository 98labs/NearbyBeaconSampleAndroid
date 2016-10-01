# Nearby Beacons Sample for Android

Demonstrates use of the
[Nearby.Messages API](https://developers.google.com/nearby/)
for communicating between
devices within close proximity of each other.

Introduction
------------

This sample allows a user to subscribe to messages published by nearby BLE devices.

To run this sample, use two or more devices to publish and subscribe messages.


Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command. Or, use "Import Project" in Android Studio.

To use this sample, follow the following steps:

1. Create a project on
[Google Developer Console](https://console.developers.google.com/). Or, use an
existing project.

2. Click on `APIs & auth -> APIs`, and enable `Nearby Messages API`.

3. Click on `Credentials`, then click on `Create new key`, and pick
`Android key`. Then register your Android app's SHA1 certificate
fingerprint and package name for your app. Use
`com.a98labs.nearbybeaconsampleandroid` for the package name.

4. Copy the API key generated, and paste as value for the
`com.google.android.nearby.messages.API_KEY` meta-data in `AndroidManifest.xml`.


Support
-------

If you've found an error in the sample, please file an issue in this repo.