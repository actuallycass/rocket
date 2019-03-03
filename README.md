[![Build Status](https://travis-ci.com/cassaundra/rocket.svg?branch=master)](https://travis-ci.com/cassaundra/rocket)
[![Maven Central](https://img.shields.io/maven-central/v/io.cassaundra/rocket.svg)](https://search.maven.org/artifact/io.cassaundra/rocket)
[![Javadocs](https://www.javadoc.io/badge/io.cassaundra/rocket.svg)](https://www.javadoc.io/doc/io.cassaundra/rocket)

<img src="Logo.png" width=192 height=192>

## Table of Contents
- [Setup](#setup)
- [Getting Started](#getting-started)
- [Colors](#colors)
- [Displaying Text](#displaying-text)
  - [Basics](#basics)
  - [Speed](#speed)
  - [Completion](#completion)
- [TODO](#todo)

## Setup

Add this to your pom.xml:

```xml
<dependency>
  <groupId>io.cassaundra</groupId>
  <artifactId>rocket</artifactId>
  <version>1.2.2</version>
</dependency>
```

Alternatively, with Gradle:

```gradle
dependencies {
  compile 'io.cassaundra:rocket:1.2.2'
}
```

## Getting Started

```kotlin
import io.cassaundra.rocket.Rocket.setButton
import io.cassaundra.rocket.Rocket.setPad

// ...

// Allow MIDI scanning to begin
Rocket.beginMidiScan()

// Listen for input events
Rocket.addListener(object : LaunchpadListener {
    override fun onPadDown(pad: Pad) {
        setPad(pad, Color.WHITE)
    }

    override fun onPadUp(pad: Pad) {
        setPad(pad, Color.OFF)
    }

    override fun onButtonDown(button: Button) {
        if(button.isTop)
            setButton(button, Color.RED)
        else
            setButton(button, Color.BLUE)
    }

    override fun onButtonUp(button: Button) {
        setButton(button, Color.OFF)
    }
})
```

Similarly, in Java...

```java
import static io.cassaundra.rocket.Rocket.setButton;
import static io.cassaundra.rocket.Rocket.setPad;

// ...

// Allow MIDI scanning to begin
Rocket.beginMidiScan();

// Listen for input events
Rocket.addListener(new LaunchpadListener() {
    public void onPadDown(@NotNull Pad pad) {
        setPad(pad, Color.WHITE);
    }

    public void onPadUp(@NotNull Pad pad) {
        setPad(pad, Color.OFF);
    }

    public void onButtonDown(@NotNull Button button) {
        if(button.isTop())
            setButton(button, Color.RED);
        else
            setButton(button, Color.BLUE);
    }

    public void onButtonUp(@NotNull Button button) {
        setButton(button, Color.OFF);
    }
});
```

Now you're on your way to creating a full Launchpad app!

## Colors

You can use custom colors by specifiying RGB int values between 0 and 63 inclusive.

```kotlin
val color = Color(42, 0, 30)
```

In Java,

```java
Color color = new Color(42, 0, 30);
```

If you want to convert an HSV value to a Launchpad color, use `Color.fromHSV`, with each value a float in the range 0 to 1.

```kotlin
Color.fromHSV(.5f, 1f, 1f)
```

## Displaying Text

### Basics

You can call the Launchpad's built-in MIDI command for displaying text with `Launchpad.displayText`. Due to a Launchpad MIDI issue, changing the color from white does not yet work.

```kotlin
Rocket.displayText(
    "Hello world!"
)
```

In Java,

```java
Rocket.displayText(
    "Hello world!"
);
```

### Speed

You can control the text scrolling speed per-character with seven different available speeds in `TextSpeed`.

```kotlin
Rocket.displayText(
    "Hello! ${TextSpeed.SPEED_1}Let's take this slower."
)
```

In Java,

```java
Rocket.displayText(
    "Hello! " + TextSpeed.SPEED_1 + "Let's take this slower."
);
```

### Completion

If you need to know when text has finished scrolling, you can use the onComplete argument.

```kotlin
Rocket.displayText(
    "Hello world!",
    onComplete = Runnable { println("Done!") }
)

```

In Java,

```java
Rocket.displayText(
    "Hello world!"
);
```

## Utils

`Pad.Util` provides several useful utility functions, like rectangles, line segments, and more.

## TODO
* Add contributing documentation
* Add support for multiple Launchpads (I have only one)
* Use Gradle instead of Maven
* Add more unit tests!
