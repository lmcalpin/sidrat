Sidrat
==
This is intended to eventually become my implementation of a recording debugger, also known as a 'time travelling debugger'.

The idea is to record every method invocation, as well as all field and Object instantiation and assignments, so you can replay
the recording later.  Because you would be simply reviewing a recording, you can jump around 'in time' to see how fields and
variables change over time; you could debug a system on a machine that can't access databases or other external systems that
the application is dependent on.

This system will record each line of code executed and track changes to local variables and fields.  It supports some basic debugging, with next and back commands, as well as a "goto" command.

There are several ways to use Sidrat:

JUnit
---
The easiest way is to use a TestRunner as follows:
~~~
@RunWith(SidratTestRunner.class)
@SidratEventStore(factory = com.sidrat.event.store.mem.InMemoryEventRepositoryFactory.class, name = "sidrat-testrepo-hello")
@SidratHistory(variables = { "sum" })
public class SampleTest {
    @Test
    public void testFailure() {
      ...
    }
}
~~~

SidratEventStore determines where we will store the recording.  Currently, we have event repositories that hold events in memory,
an HSQLDB database, as well as via JPA to use a production quality database.

The SidratTestRunner will cause all local variables to be dumped to standard output when the test finishes executing if there is a test failure.

SidratHistory will allow you to specify certain local variables to have the history of all tracked updates dumped to standard output.

This test runner is mostly intended as a way to explore potential uses of Sidrat and we don't currently recommend that it be used in any production test framework.

Manual Recording
---
You can also record Java code directly:

~~~
EventRepositoryFactory factory = new HsqldbEventRepositoryFactory();
SidratRecorder recorder = SidratRegistry.instance().newRecorder();
recorder.store(factory.store(repositoryName));
recorder.record(() -> {
    int sum = 0;
    for (int i = 0; i < 5; i++) {
        sum += 5;
    }
    System.out.println(sum);
});
~~~

and then you can replay the recordings later:

~~~
EventRepositoryFactory factory = new HsqldbEventRepositoryFactory();
SidratReplay replay = new SidratReplay(factory.reader(repositoryName));
replay.gotoEvent(1);
~~~

Warning
---
This is *very far* from being complete.  It is an experiment and a work in progress.
