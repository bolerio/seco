# Seco

Seco is a collaborative scripting development environment for the Java platform. You can write code in many JVM scripting languages. The code editor in Seco is based on the Mathematica notebook UI, but the full GUI is richer and much more ambitious. In a notebook, you can mix rich text with code and output, including interactive components created by your code. This makes Seco into a live environment because you can evaluate expression and immediately see the changes to your program. 

## Jump To:

* [Latest Release](https://github.com/bolerio/seco/wiki/Latest-Release) - download links etc.
* [Installing and Starting Seco](https://github.com/bolerio/seco/wiki/Installing-and-Starting-Seco) - it's basically unpack and run 'run.sh', but here are some details.
* [Google group for discussion](https://groups.google.com/forum/#!forum/scriba) - post questions & ideas there rather than writing personally to me.
* [Become a Seco Developer](https://github.com/bolerio/seco/wiki/Become-a-Seco-Developer) - yes, come and help!

## Take a Glance 
Here is a screenshot of the full GUI, though out of the box you'd see a simplified version with a single tabbed pane:

![Alt text](http://kobrix.com/images/secofullshot.png "Seco Screenshot")

## What Can I Use It For?

From short scripts for administrative tasks to complete libraries in your favorite JVM language, to interactive scripted applications, you can write and share any code that can run on the Java platform. You can also explore and learn APIs, programming languages and algorithms. You can rely on Seco for complex data analysis tasks using your favorite analytics or machine learning library combined your favorite database. 

## What Can It Do? 

With Seco you write notebooks which are structured files mixing code, documentation and output. The concept comes from the Mathematica system. You can mix different programming languages integrated in a single runtime. You can create GUI interfaces for various tasks and organize them in zoomable, nested containers. The input of any computation is an evaluatable cell in some scripting language. The output of any computation is also cell that can contain any type of Java object and that can also be moved and attached to any other piece of the environment. The connection between input and output is preserved. You can share your work at the individual cell level with others in real-time through a P2P network, or by exporting whole notebooks to files.

## What It Wants To be?

A collaborative live development environment for building large-scale systems based on the evolution, sharing and reuse of fine-grained software artifacts. A detailed exposition of the long term vision can be read in the [Rapid Software Evolution paper] (http://kobrix.com/documents/rse.pdf).

## Brief History

Seco is more than a decade old (circa 2004). Initially called _Scriba_, it was funded and developed by [Kobrix Software, Inc.](http://www.kobrix.com) with the double goal of being a practical programming tool for daily use, initially complementing standard IDEs, and working towards achieving a new vision for knowledge-driven programming as outlined in the above mentioned paper. While it matured quickly, as funds dried out, development staled. Currenly the project is being maintained sporadically. Most of the code was written by Konstantin Vandev. It is currently maintained by Borislav Iordanov (@bolerio). Actively seeking help! 

## Comparison with Beaker/IPython/Jupyter

In recent years, a few comparable tools have emerged around the IPython web based notebook interface. Those look great and they undoubtedly share some overlap with what Seco has to offer. But here is how they differ and why you should care about Seco (i.e. give it a try, give feedback, help out with development and ideas):

* Seco's foundation is HyperGraphDB. This is not immediately apparent, but everything you do is automatically persisted, sort of like in Smalltalk. Except, instead of saving a binary image and trying to work hand in hand with the JVM to bring it back alive, Seco's model is one of maintaining dependencies between inputs and outputs where initial input cells bootstrap the evaluation until the full interactice environment is recreated. The ultimate goal has always been a knowledge-driven P2P programming (with an evolutionary process on top). The JVM is a problem, but there is practically no other choice if one wants something immediately useful.
* This point is worth understanding well: the very GUI of Seco is represented and stored in an embedded HyperGraphDB instance. The notebooks, the various types of cells are HyperGraphDB atoms so that they can be shared and modified at a fine grained level. A true live environment is hard to pull off within the JVM, but one can get close enough because one doesn't have to represent objects in a JVM serialized form, on can re-evaluate and re-create things at runtime.
* From a more practical perspective, those newer tools are web-based while Seco is a plain desktop Java application. An advantage of IPython for example is that it looks prettier at the moment because Seco is written in Swing. A rewritten of Seco in JavaFX would bring the graphics on par. 
* Also notably, a crucial difference is in the evaluation architecture: IPython based tools are client-server, which implies that the environment where the computation takes places is *separate* from the environment where user interaction happens. This leads to rather different possibility spaces. And it is no accident. It is a conscious decision for Seco to be part of the computation. Client-server, cloud based computational resources can always be plugged as evaluation engines for Seco, of course. But I'd rather embed a browser or a web rendering engine inside the enviorment instead of running within a browser. That's because the ultimate long term dream remains taking a shot at an alternative P2P, crowd-sourced programming model.

