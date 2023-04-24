# Zoomable

![Build & test](https://github.com/mxalbert1996/Zoomable/actions/workflows/build.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/com.mxalbert.zoomable/zoomable)](https://search.maven.org/artifact/com.mxalbert.zoomable/zoomable)

Content zooming with dragging, double tap and dismiss gesture support for Compose Multiplatform.
Supports Android, iOS and desktop.

The demo for Android can also be installed from [Google Play](https://play.google.com/store/apps/details?id=com.mxalbert.zoomable.sample).

## Features

- Dragging, double tap and vertical swipe-to-dismiss gesture support
- Over-zoom effect
- Seamless integration with horizontal pager

## Download
```Kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.mxalbert.zoomable:zoomable:<version>")
}
```

<details>
  <summary>Snapshot builds</summary>

[Snapshot versions](https://s01.oss.sonatype.org/content/repositories/snapshots/com/mxalbert/zoomable/zoomable/) are available at Sonatype OSSRH's snapshot repository. These are updated on every commit to `main`.
To use it:
```Kotlin
repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")  // build.gradle.kts
    maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots' }  // build.gradle
}

dependencies {
    implementation("com.mxalbert.zoomable:zoomable:<version>-SNAPSHOT")
}
```
</details>

## Usage
```Kotlin
Zoomable {
    val painter = painterResource(R.drawable.image)
    val size = painter.intrinsicSize
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .aspectRatio(size.width / size.height)
            .fillMaxSize()
    )
}
```

Note that `Modifier.aspectRatio()` is needed in order to remove the extra space around the image
when the image is zoomed.

For how to use `Zoomable` with [Coil](https://github.com/coil-kt/coil), see
[the sample](sample/shared/src/androidMain/kotlin/com/mxalbert/zoomable/sample/ZoomableApp.android.kt#L26-L41).

## Notes

Improvements and behavior changes compared to [the original version](https://github.com/Tlaster/Zoomable):

- This version is mainly intended to be used to show images. If the image is larger than the screen, when zoomed, the original version will show the upscaled version of the downscaled image, while this version will directly scale the image to the zoomed size, which has higher quality.
- Contents will follow finger position when zooming with two fingers.
- Supports animating scale level and translation at the same time.
- Other features

## License
```
MIT License

Copyright (c) 2021 Tlaster
Copyright (c) 2022 Albert Chang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
