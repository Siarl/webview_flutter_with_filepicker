# Explanation

Currently, `webview_flutter` does not support uploading files to websites on Android.
This funcionality [will be added in version 2.0.3](https://github.com/flutter/plugins/pull/3225/files) 
through the follwing PR: [flutter/plugins#3225](https://github.com/flutter/plugins/pull/3225).

This repo contains a quick fix based on the suggestions in
[this issue comment](https://github.com/flutter/flutter/issues/27924#issuecomment-783229087) 
and the following gists:
- [FlutterWebview.java](https://gist.github.com/tneotia/ccdb84ba66d6863980b9d558fb95312e)
- [newActivity.java (FilePickerActivity.java)](https://gist.github.com/tneotia/adc61196cfa59a5a7bdef6efb0ab5883)

I modified these files to:
- use [EasyPermissions](https://github.com/googlesamples/easypermissions)
- use material theme for dialogs
- fit my usecase a bit better

For other usecases it might be usefull to clone this and mess with 
`FilePickerActivity.java`.

Note that this version of `webview_flutter` is based on version 1.0.7, 
so there are some existing issues fixed in later releases.

I'm not going to maintain this actively and will switch to the official fix 
once that comes out (and once there are no dependency conflicts).

### How to use

In your `pubspec.yaml`, replace

```yaml
dependencies:
  webview_flutter: ^1.0.7
```

with:

```yaml
dependencies:
  webview_flutter:
    git: git@github.com:Siarl/webview_flutter_with_filepicker.git
```

---

# WebView for Flutter

A Flutter plugin that provides a WebView.

On iOS the WebView widget is backed by a [WKWebView](https://developer.apple.com/documentation/webkit/wkwebview);
On Android the WebView widget is backed by a [WebView](https://developer.android.com/reference/android/webkit/WebView).

## Usage
Add `webview_flutter` as a [dependency in your pubspec.yaml file](https://flutter.io/platform-plugins/).

You can now include a WebView widget in your widget tree. See the
[WebView](https://pub.dev/documentation/webview_flutter/latest/webview_flutter/WebView-class.html)
widget's Dartdoc for more details on how to use the widget.



## Android Platform Views
The WebView is relying on
[Platform Views](https://flutter.dev/docs/development/platform-integration/platform-views) to embed
the Android’s webview within the Flutter app. By default a Virtual Display based platform view
backend is used, this implementation has multiple
[keyboard](https://github.com/flutter/flutter/issues?q=is%3Aopen+label%3Avd-only+label%3A%22p%3A+webview-keyboard%22).
When keyboard input is required we recommend using the Hybrid Composition based platform views
implementation. Note that on Android versions prior to Android 10 Hybrid Composition has some
[performance drawbacks](https://flutter.dev/docs/development/platform-integration/platform-views#performance).

### Using Hybrid Composition

To enable hybrid composition, set `WebView.platform = SurfaceAndroidWebView();` in `initState()`.
For example:

```dart
import 'dart:io';

import 'package:webview_flutter/webview_flutter.dart';

class WebViewExample extends StatefulWidget {
  @override
  WebViewExampleState createState() => WebViewExampleState();
}

class WebViewExampleState extends State<WebViewExample> {
  @override
  void initState() {
    super.initState();
    // Enable hybrid composition.
    if (Platform.isAndroid) WebView.platform = SurfaceAndroidWebView();
  }

  @override
  Widget build(BuildContext context) {
    return WebView(
      initialUrl: 'https://flutter.dev',
    );
  }
}
```

`SurfaceAndroidWebView()` requires [API level 19](https://developer.android.com/studio/releases/platforms?hl=th#4.4). The plugin itself doesn't enforce the API level, so if you want to make the app available on devices running this API level or above, add the following to `<your-app>/android/app/build.gradle`:

```gradle
android {
    defaultConfig {
        // Required by the Flutter WebView plugin.
        minSdkVersion 19
    }
  }
```
