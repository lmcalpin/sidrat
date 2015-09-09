Sidrat
==

This is intended to eventually become my implementation of a recording debugger, also known as a 'time travelling debugger'.

The idea is to record every method invocation, as well as all field and Object instantiation and assignments, so you can replay
the recording later.  Because you would be simply reviewing a recording, you can jump around 'in time' to see how fields and
variables change over time; you could debug a system on a machine that can't access databases or other external systems that
the application is dependent on.

This system can record and replay events at and supports local variable and field tracking.
It supports some basic debugging, with next and back commands, as well as a "goto" (any time period) command.

There are several ways to use Sidrat:

 - the easiest way is to use a TestRunner as follows:
 ~~~
@RunWith(SidratTestRunner.class)
public class SampleTest {
    @Test
    public void testFailure() {
      ...
    }
}
 ~~~
 - you can use it as a java agent to record other program executions
 - you can use SidratRecorder.record method and pass in a Runnable that you want recorded
 - the Sidrat class will launch other main methods from the command line and record the program execution

This is *very far* from being complete.  It is a work in progress.

If you are interested in the concept, look at the ["Omniscient Debugger"](http://www.lambdacs.com/debugger/).
My actual implementation is quite different, but the idea is basically the same.
