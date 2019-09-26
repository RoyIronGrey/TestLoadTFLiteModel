最近因为一个研究课题中发现TensorFlow-lite存在一个小问题(如下 **现象出发** 小节所述)，所以需要对TensorFlow-lite源码进行研究分析，第一步当然就是要对源码进行编译咯。
# 现象出发

## 系统信息

- OS Platform and Distribution : Windows 10 and Android 9.0

- Mobile device : Xiaomi 8

- TensorFlow version : org.tensorflow:tensorflow-lite:0.0.0-nightly (using 1.11.0 has the same problem)

- Model: ResNet152, ResNet18  convert from Pytorch and MobileNet-V1 from [tensorflow-lite demo](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/lite/java/demo)

- Project: [TestLoadTFLiteModel]([https://github.com/RoyIronGrey/TestLoadTFLiteModel](https://github.com/RoyIronGrey/TestLoadTFLiteModel))  (里面自带了MobileNet模型，如果想运行ResNet152和和ResNet18模型，可以联系15665512229@163.com, 如果想知道ResNet152和和ResNet18模型的转换可以参考[此处](https://github.com/tensorflow/tensorflow/issues/27807))

![Project.PNG](https://upload-images.jianshu.io/upload_images/19511012-173304c1a29ef90b.PNG?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


转换模型的方式可以参考[这个网址](https://github.com/tensorflow/tensorflow/issues/27807) 

## Logs from Android 9.0

Load ResNet-152 (222 MB)
D/TimeCost: Timecost to load model file: **0 ms**
D/TimeCost: Created a Tensorflow Lite Image Classifier.
Load MobileNet-V1 (16.1 MB)
D/TimeCost: Timecost to load model file: **0 ms** 
D/TimeCost: Created a Tensorflow Lite Image Classifier.
Load ResNet-18 (42.8MB)
D/TimeCost: Timecost to load model file: **0 ms** 
D/TimeCost: Created a Tensorflow Lite Image Classifier.

Inference ResNet-152 for the first time 
D/TimeCost: Timecost to put values into ByteBuffer: **29 ms**
D/TimeCost: Timecost to run model inference: **1681 ms** 
Inference ResNet-152 for the second time 
D/TimeCost: Timecost to put values into ByteBuffer: **3 ms** 
D/TimeCost: Timecost to run model inference: **589 ms** 
Inference ResNet-152 for the third time 
D/TimeCost: Timecost to put values into ByteBuffer: **3 ms** 
D/TimeCost: Timecost to run model inference: **591 ms** 

Inference MobileNet-V1 for the first time
D/TimeCost: Timecost to put values into ByteBuffer: **34 ms**
D/TimeCost: Timecost to run model inference: **134 ms** 
Inference MobileNet-V1 for the second time
D/TimeCost: Timecost to put values into ByteBuffer: **3 ms** 
D/TimeCost: Timecost to run model inference: **56 ms** 
Inference MobileNet-V1 for the third time 
D/TimeCost: Timecost to put values into ByteBuffer: **4 ms**
D/TimeCost: Timecost to run model inference: **58 ms** 

Inference ResNet-18 for the first time 
D/TimeCost: Timecost to put values into ByteBuffer: **10 ms** 
D/TimeCost: Timecost to run model inference: **396 ms**
Inference ResNet-18 for the second time
D/TimeCost: Timecost to put values into ByteBuffer: **2 ms**
D/TimeCost: Timecost to run model inference: **108 ms**
Inference ResNet-18 for the third time
D/TimeCost: Timecost to put values into ByteBuffer: **2 ms**
D/TimeCost: Timecost to run model inference: **85 ms**

从上述 **Logs from Android 9.0** 可以看出**各个模型在第一次Inference的时候都花了较多时间(相比后两次)，这就是所发现的小问题**

下面是 Load Model 和 Inference 的关键代码 ( **class Classifier** 所在位置如系统信息中Project: TestLoadTFLiteModel 目录图所示)

### Load Model

```java
public abstract class Classifier {
    ......
	private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getLabelPath());
        FileInputStream inputStream = new
            FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset,
                               declaredLength);
	}
	long startTime = SystemClock.uptimeMillis();
	(MappedByteBuffer)tfliteModel = loadModelFile(activity);
	//Only when run method load() of MappedByteBuffer Object, this buffer's content is 		being loaded into physical memory
	//tfliteModel.load();
	long endTime = SystemClock.uptimeMillis();
	Log.d(TAG, "Timecost to load model file: " + Long.toString(endTime - startTime));
	(org.tensorflow.lite.Interpreter.Options)Interpreter.Options tfliteOptions = new 
        Interpreter.Options()
	(org.tensorflow.lite.Interpreter)tflite = new Interpreter(tfliteModel,
        tfliteOptions);
    ......
}
```

### Inference

```java
public abstract class Classifier {
    ......
	protected float[] runInference() {
   		//labelProbArray = new float[1][getNumLabels()]
   		tflite.run(Data, labelProbArray);
   		return labelProbArray[0];
	}
	public float[] classifyFrame(float input[]) {
   		if (tflite == null) {
       	Log.e(TAG, "Image classifier has not been initialized; Skipped.");
    	}
   		convertArrayToByteBuffer(input);
   		// Here's where the magic happens!!!
   		long startTime = SystemClock.uptimeMillis();
   		float[] result = runInference();
   		long endTime = SystemClock.uptimeMillis();
   		Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime-
                                                                       startTime));
   		return result;
	}
    ......
}
```

### 分析现象

从上面 **Logs from Android 9.0** 可以看到加载模型的只花了很少的时间，这是因为 **MappedByteBuffer** 对象在创建的时候并没有将文件拷到物理运行内存(DRAM)当中，只有当执行了 **load()** 方法的时候才会真正加载(如 **Load Model** 中注释的代码所示，细节可以参考[官方API](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/nio/MappedByteBuffer.html))

从而考虑到可能是在执行 **runInference()** 方法中的 **tflite.run(Data, labelProbArray)** 的内部代码执行了 **load()**   方法或与之等价的方法，进入 **tflite.run(Data, labelProbArray)** 后得到如下内容：

```java
public final class Interpreter implements AutoCloseable {
    ......
	public void run(Object input, Object output) {
    	Object[] inputs = new Object[]{input};
    	Map<Integer, Object> outputs = new HashMap();
     	outputs.put(0, output);
     	this.runForMultipleInputsOutputs(inputs, outputs);
	}

	public void runForMultipleInputsOutputs(Object[] inputs, @NonNull Map<Integer, Object> outputs) {
     	this.checkNotClosed();
    	//NativeInterpreterWrapper wrapper;
     	this.wrapper.run(inputs, outputs);
	}
    ......
}
```

由上面的 **runForMultipleInputsOutputs(...)** 方法中的内容可知执行下一层操作的一个成员变量 **wrapper** 的 **run(inputs, outputs)** 方法，而从 **wrapper** 所属类 **NativeInterpreterWrapper** 可知这是一个调用了 **Native** 方法的类，也就是所谓的“胶水代码”，进入 **NativeInterpreterWrapper** 发现事实也是如此：

```java
final class NativeInterpreterWrapper implements AutoCloseable {
    ......
void run(Object[] inputs, Map<Integer, Object> outputs) {
        this.inferenceDurationNanoseconds = -1L;
        if (inputs != null && inputs.length != 0) {
            if (outputs != null && !outputs.isEmpty()) {
                for(int i = 0; i < inputs.length; ++i) {
                    Tensor tensor = this.getInputTensor(i);
                    int[] newShape = tensor.getInputShapeIfDifferent(inputs[i]);
                    if (newShape != null) {
                        this.resizeInput(i, newShape);
                    }
                }

                boolean needsAllocation = !this.isMemoryAllocated;
                if (needsAllocation) {
                    allocateTensors(this.interpreterHandle, this.errorHandle);
                    this.isMemoryAllocated = true;
                }

                for(int i = 0; i < inputs.length; ++i) {
                    this.getInputTensor(i).setTo(inputs[i]);
                }

                long inferenceStartNanos = System.nanoTime();
                run(this.interpreterHandle, this.errorHandle);
                long inferenceDurationNanoseconds = System.nanoTime()-
                    inferenceStartNanos;
                if (needsAllocation) {
                    for(int i = 0; i < this.outputTensors.length; ++i) {
                        if (this.outputTensors[i] != null) {
                            this.outputTensors[i].refreshShape();
                        }
                    }
                }

                Iterator var13 = outputs.entrySet().iterator();

                while(var13.hasNext()) {
                    Entry<Integer, Object> output = (Entry)var13.next();
                
                this.getOutputTensor((Integer)output.getKey()).copyTo(output.getValue());
                }

                this.inferenceDurationNanoseconds = inferenceDurationNanoseconds;
            } else {
                throw new IllegalArgumentException("Input error: Outputs should not be null or empty.");
            }
        } else {
            throw new IllegalArgumentException("Input error: Inputs should not be null or empty.");
        }
    }

    private static native void run(long interpreterHandle, long errorHandle);
    ......
}
```

**NativeInterpreterWrapper** 中调用了 **run(long interpreterHandle, long errorHandle)** 的 Native 方法，所以想要继续深入则需要看相关 C/C++实现源码

# 相关配置

为了后期对源码的修改和重新构建后方便引入到测试项目中进行调试，之后将采用 Ubuntu 作为开发环境（参考[https://blog.csdn.net/mingchong2005/article/details/80567511](https://blog.csdn.net/mingchong2005/article/details/80567511)）。以 Java 为基础的 Android 开发方式以及 Android Studio 的使用方法此处略过。

在 Ubuntu 下安装 Android studio 创建工程连接手机调试一开始会提示 insufficient permissions for device而无法成功，进行如下配置即可

1、配置adb的环境变量

进入终端，输入

```shell
sudo gedit ~/.bashrc
```

在文件的最后追加：

```shell
export PATH=$PATH:/XXXXXX/android-sdk-linux/tools/
export PATH=$PATH:/XXXXXX/android-sdk-linux/platform-tools/
```

**XXXXXX为android-sdk-linux所在目录的上层目录，Android Studio默认会将android-sdk-linux命名为Sdk**

保存更新

```shell
source ~/.bashrc
```

2、切换到root，再次运行步骤1

然后重启adb服务

```shell
adb kill-server
adb start-server
```

（1）插上手机，终端输入：

```shell
lsusb
```

会列出所有占用usb设备。
从中找出你自己的设备：

```shell
Bus 002 Device 002: ID 8087:0024 Intel Corp. Integrated Rate Matching Hub
Bus 002 Device 001: ID 1d6b:0002 Linux Foundation 2.0 root hub
Bus 001 Device 010: ID 0bb4:0c03 HTC (High Tech Computer Corp.) //记住这一行
Bus 001 Device 002: ID 8087:0024 Intel Corp. Integrated Rate Matching Hub
Bus 001 Device 001: ID 1d6b:0002 Linux Foundation 2.0 root hub
Bus 004 Device 001: ID 1d6b:0003 Linux Foundation 3.0 root hub
Bus 003 Device 003: ID 1c4f:0026 SiGma Micro Keyboard
Bus 003 Device 005: ID 093a:2510 Pixart Imaging, Inc. Optical Mouse
Bus 003 Device 001: ID 1d6b:0002 Linux Foundation 2.0 root hub
```

如果一下子不知道是哪一个，就把手机拔了在lsusb，比较一下就知道是哪个了。

（2）继续终端输入

```shell
sudo gedit /etc/udev/rules.d/51-android.rules
```

加入如下语句 (**根据你自己手机的 idVendor 和 idProduct输入**)

```shell
SUBSYSTEM=="usb", ATTRS{idVendor}=="0bb4", ATTRS{idProduct}=="0c03",MODE="0666"
```

保存。

（3）终端输入

```shell
sudo chmod a+rx /etc/udev/rules.d/51-android.rules
sudo service udev restart
```

（4）拔掉usb，重新连上(很重要)，再执行：

```shell
adb devices
```

这样应该就能看设备.

```shell
xxxx@xxxx-xxxxxx:~$ adb devices
List of devices attached
0123456789ABCDEF device
```

# 源码分析

## 项目目录

从github上下载[tensorflow源码](https://github.com/tensorflow/tensorflow/)，其顶层目录结构如下(tensorflow-lite是作为tensorflow的一个子项目)：

![tensorflow-master.png](https://upload-images.jianshu.io/upload_images/19511012-884696313d4e2bc1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


通过上述 **NativeInterpreterWrapper** 类中的native方法名搜索得到 

**tensorflow-master/tensorflow/lite/java** 目录应该是对 tensorflow-lite 运行过程进行分析的入口，因为其包含了 andorid 端调用的 **demo** 源码以及构建 **arr**(Android库项目的二进制归档文件，包含所有资源，class以及res资源文件等，打包好后可类似于 jar 包以供调用) 的脚本文件 **build_aar_for_release.sh **。

## 构建 Tensorflow-lite 包 for Android 

### 构建 tensorflow-lite arr

从 **build_aar_for_release.sh** 中可得知编译得到 tensorflow-lite arr 的工具为 **bazel** 

```shell
24 BUILDER=bazel
```

**bazel** 的原理和使用方法可以参考[官网](https://docs.bazel.build/versions/master/bazel-overview.html)，也可以参考[此博客](https://www.jianshu.com/p/727cbf544c47)

从 **build_aar_for_release.sh** 的以下命令可知执行此 shell 脚本应该在 tensorflow 的根目录下进行，即 **tensorflow-master/**

```shell
32 test -d $BASEDIR || (echo "Aborting: not at top-level build directory"; exit 1)
```

但如果你直接执行 **./tensorflow/lite/java/build_aar_for_release.sh** 命令则会报如下错误：

```shell
ERROR: /root/.cache/bazel/_bazel_root/48afd7ce3ec2abb0075b846a0152785f/external/bazel_tools/tools/android/BUILD:385:1: Executing genrule @bazel_tools//tools/android:no_android_sdk_repository_error failed (Exit 1)
This build requires an Android SDK. Please add the android_sdk_repository rule to your WORKSPACE.
Target //tensorflow/lite/java:tensorflowlite.aar failed to build
Use --verbose_failures to see the command lines of failed build steps.
INFO: Elapsed time: 0.741s, Critical Path: 0.00s
INFO: 0 processes.
FAILED: Build did NOT complete successfully
```

这是因为没有配置 **Android SDK** 和 **Android NDK** 的原因，所以需要在根目录的 **WORKSPACE **文件中添加如下的规则代码(SDK和NDK的 **path** 根据实际情况进行修改):

```shell
# Uncomment and update the paths in these entries to build the Android demo.
android_sdk_repository(
name = "androidsdk",
api_level = 29,
# Ensure that you have the build_tools_version below installed in the
# SDK manager as it updates periodically.
#build_tools_version = "29.0.2",
# Replace with path to Android SDK on your system
path = "/home/edge/Android/Sdk",
)

android_ndk_repository(
name="androidndk",
path="/home/edge/Workspace/android-ndk-r20",
# This needs to be 14 or higher to compile TensorFlow.
# Please specify API level to >= 21 to build for 64-bit
# archtectures or the Android NDK will automatically select biggest
# API level that it supports without notice.
# Note that the NDK version is not the API level.
api_level=29)
```

在做了以上配置之后再打开 **build_aar_for_release.sh**  进行编辑，可以在57-61行(可能会有细微差别)处看到如下代码：

```shell
build_basic_aar $TMPDIR
build_arch arm64-v8a arm64-v8a $TMPDIR
build_arch armeabi-v7a armeabi-v7a $TMPDIR
build_arch x86 x86 $TMPDIR
build_arch x86_64 x86_64 $TMPDIR
```

在这里我们可以将我们不需要的架构下的 aar 构建命令注释

注释后再次运行 **./tensorflow/lite/java/build_aar_for_release.sh** 可在根目录得到 **tflite-1.0.aar** (或者你在 **build_aar_for_release.sh** 进行修改后得到的其他命名)文件。

### 调用 tensorflow-lite arr

构建好之后就可以将 aar 文件放入项目中调用了，具体调用方式如下：

我们以 Project 的方式展开测试项目  **TestLoadTFLiteModel** ，项目目录如下：

![Project-2.png](https://upload-images.jianshu.io/upload_images/19511012-1d64db83b5c88a64.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


此时我们需要将之前生成的 **tflite-1.0.aar** 文件拷入到 **libs** 目录下

然后在 **build.gradle**(Module:app) 文件中添加如下代码：

```gradle
//和 dependencies 同一级
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
	//......
	//你可以通过注释来启用不同的版本(本地或远程)
    implementation(name:'tflite-1.0', ext:'aar')
//    implementation 'org.tensorflow:tensorflow-lite:1.14.0'
//    implementation 'org.tensorflow:tensorflow-lite:0.0.0-nightly'
	//......
}
```

完成以上操作后就可以 build and run 测试项目 **TestLoadTFLiteModel**  了
