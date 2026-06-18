# Solana Event System

Lightweight Java event system designed for modular projects and game plugins.

## Features

- Fast registration and event dispatch
- Annotation-based listeners (`@EventHandler`)
- Priority support for event handlers
- Support for stoppable events (`EventStoppable`)
- Reflection-based implementation with simple architecture
- Fully independent core (no Minecraft dependencies)
- Easy integration into any Java project

## Installation

### Gradle

```groovy
dependencies {
    implementation 'your.group:solana-eventsystem:1.0.0'
}
Usage
1. Create an event
public class TestEvent extends Event {
    private final String message;

    public TestEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
2. Create listener
public class TestListener {

    @EventHandler(priority = 1)
    public void onTest(TestEvent event) {
        System.out.println("Received: " + event.getMessage());
    }
}
3. Register listener
EventManager eventManager = new EventManager();
eventManager.register(new TestListener());
4. Call event
EventManager.callEvent(new TestEvent("Hello World"));
Stoppable events

If your event implements EventStoppable, execution can be stopped during dispatch.

public class MyEvent extends Event implements EventStoppable {

    private boolean stopped;

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        this.stopped = true;
    }
}

Event dispatch will stop when isStopped() returns true.

EventHandler annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    byte priority() default 0;
}

Higher priority values are executed first.

Event priority
Priority	Execution order
higher value	executed earlier
lower value	executed later
Design notes
Uses reflection for method invocation
fastutil-based internal storage
static event dispatch model
designed for modular systems and plugins
Limitations
Not fully thread-safe (single-thread dispatch assumed)
Uses reflection (not zero-cost)
Events are matched by exact class type only

Author

Fl1ntDev


3. Сделай:
```bash
git add README.md
git commit -m "add README"
git push
