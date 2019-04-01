## JMH JAVA基准测试

> 通过本篇文章你可以直到一下内容
>
> - 什么是JMH
> - JMH适用场景
> - JMH入门级demo
> - **JMH如何测试业务代码**

### 什么是JMH

> JMH，即Java Microbenchmark Harness，这是专门用于进行代码的微基准测试的一套工具API。JMH 由 OpenJDK/Oracle 里面那群开发了 Java 编译器的大牛们所开发 。何谓 Micro Benchmark 呢？ 简单地说就是在 method 层面上的 benchmark()，精度可以精确到微秒级。

### 使用场景

- 实现同样一种功能有多种方式时，可以通过JMH可以知道使用哪种方式更加适合
- 可以通过JMH知道方法的性能如何

### JMH入门级demo

#### 独立项目运行demo

##### 操作步骤

- 1.使用maven生成demo项目

```shell
mvn archetype:generate \
    -DinteractiveMode=false \
    -DarchetypeGroupId=org.openjdk.jmh \
    -DarchetypeArtifactId=jmh-java-benchmark-archetype \
    -DgroupId=io.four \
    -DartifactId=jmh-demo \
    -Dversion=1.0
```

通过上面的操作在当前目录中已经创建好了一个项目，导入到idea里面

- 2.编写测试方法

```java
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BenchmarkMode(Mode.AverageTime)// 测试方法平均执行时间
@OutputTimeUnit(TimeUnit.MICROSECONDS)// 输出结果的时间粒度为微秒
public class MyBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(MyBenchmark.class);

    @Benchmark
    public void stringAdd() {
        String str = "";
        for(int i = 0 ; i < 100 ; i++){
            str += "abc"+i;
        }
    }

    @Benchmark
    public void stringBufferAdd() {
        StringBuffer str = new StringBuffer();
        for(int i = 0 ; i < 100 ; i++){
            str.append("abc").append(i);
        }
    }

    public static void main(String[] args) throws RunnerException {
        // 可以通过注解
        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .warmupIterations(3) // 预热3次
                .threads(8) // 10线程并发
                .forks(1)//模拟一个线程执行
                .build();
        new Runner(opt).run();
    }

}
```

上面的测试是验证使用String进行字符串拼接和使用StringBuffer进行字符串拼接两种方式哪种效率更高。得到的测试结果是求的平均响应时间

- 执行报告：
![image.png](https://upload-images.jianshu.io/upload_images/7686832-f1fd18d83b804c57.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![image.png](https://upload-images.jianshu.io/upload_images/7686832-043f899d3fbb7f7c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
可以看到使用StringBuffer进行拼接的效果是使用String的大概6倍，当然如果调高for循环次数这个差别会更加明显，这里就不做更进一步的测试。

> 对于上面的注解不明白没有关系，后面会给出解释。

#### 在项目中使用

如果你要在自己的项目中嵌套使用JMH，可以将上面独立项目的pom文件写到你自己项目中去。pom文件如下：

```xml
<dependencies>
        <dependency>
              <groupId>org.slf4j</groupId>
              <artifactId>slf4j-api</artifactId>
              <version>1.7.7</version>
        </dependency>
        <dependency>
              <groupId>ch.qos.logback</groupId>
              <artifactId>logback-classic</artifactId>
              <version>1.0.11</version>
        </dependency>
        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-core</artifactId>
            <version>${jmh.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>${jmh.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.mph</groupId>
            <artifactId>mph-user-service</artifactId>
            <version>1.0.37-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.5.7</version>
        </dependency>
    </dependencies>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!--
            JMH version to use with this project.
          -->
        <jmh.version>1.21</jmh.version>

        <!--
            Java source/target to use for compilation.
          -->
        <javac.target>1.8</javac.target>

        <!--
            Name of the benchmark Uber-JAR to generate.
          -->
        <uberjar.name>benchmarks</uberjar.name>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.1</version>
                <configuration>
                    <compilerVersion>${javac.target}</compilerVersion>
                    <source>${javac.target}</source>
                    <target>${javac.target}</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <finalName>${uberjar.name}</finalName>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>org.openjdk.jmh.Main</mainClass>
                                </transformer>
                            </transformers>
                            <filters>
                                <filter>
                                    <!--
                                        Shading signed JARs will fail without this.
                                        http://stackoverflow.com/questions/999489/invalid-signature-file-when-attempting-to-run-a-jar
                                    -->
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
        <pluginManagement>
            <plugins>
                <plugin>
                    <artifactId>maven-clean-plugin</artifactId>
                    <version>2.5</version>
                </plugin>
                <plugin>
                    <artifactId>maven-deploy-plugin</artifactId>
                    <version>2.8.1</version>
                </plugin>
                <plugin>
                    <artifactId>maven-install-plugin</artifactId>
                    <version>2.5.1</version>
                </plugin>
                <plugin>
                    <artifactId>maven-jar-plugin</artifactId>
                    <version>2.4</version>
                </plugin>
                <plugin>
                    <artifactId>maven-javadoc-plugin</artifactId>
                    <version>2.9.1</version>
                </plugin>
                <plugin>
                    <artifactId>maven-resources-plugin</artifactId>
                    <version>2.6</version>
                </plugin>
                <plugin>
                    <artifactId>maven-site-plugin</artifactId>
                    <version>3.3</version>
                </plugin>
                <plugin>
                    <artifactId>maven-source-plugin</artifactId>
                    <version>2.2.1</version>
                </plugin>
                <plugin>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.17</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
```

**进行适当的修改即可**

### 压测项目中的方法

#### 使用独立的项目进行测试

> 下面这个例子是压测dubbo接口，目前微服务中接口都可以按照下面套路进行压测

- 1.导入相应的jar包

```xml
				<!--下面是dubbo服务的jar包，一般接口定义都在这个jar包里面-->
				<dependency>
            <groupId>com.mph</groupId>
            <artifactId>mph-user-service</artifactId>
            <version>1.0.37-SNAPSHOT</version>
        </dependency>
				<!--dubbo依赖的jar包-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.5.7</version>
        </dependency>
```

- 2.添加spring配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://code.alibabatech.com/schema/dubbo
       http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
    <dubbo:application name="demo-consumer"/>
    <dubbo:registry  address="zookeeper://192.168.19.124:2181"/>
    <import resource="classpath:user-dubbo-reference-local.xml"/>
</beans>
```

上面是一个基本的dubbo消费端的基本配置有了上面配置之后就可以使用spring容器帮我们生成代理类

- 3.编写测试类

```java
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.mph.coreapi.user.service.LoginUserService;
import com.rogrand.coreapi.user.entity.BizEnterpriseVipLog;
import com.rogrand.coreapi.user.service.BizEnterpriseVipLogService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: chao.zhu
 * @description:
 * @CreateDate: 2019/04/01
 * @Version: 1.0
 */
@BenchmarkMode(Mode.AverageTime) //压测模式是平均执行时间
@Warmup(iterations = 3)  //预热3轮
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)//轮询次数10次，每次5秒
@Threads(8)//开8个线程执行
@Fork(1)//开一个进程
@OutputTimeUnit(TimeUnit.MILLISECONDS)//统计结果以毫秒为单位
@State(Scope.Benchmark)//整个JMH共享，与Setup同用
public class JmhByUserService {

    private BizEnterpriseVipLogService bizEnterpriseVipLogService;
    private LoginUserService loginUserService;
		//这个是初始化方法，JMH会自动帮我们先调用这个方法对属性进行初始化动作。下面初始化了两个dubbo服务
    @Setup
    public void init(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("application-dubbo.xml");
        bizEnterpriseVipLogService = ac.getBean("bizEnterpriseVipLogService",BizEnterpriseVipLogService.class);
        loginUserService = ac.getBean("loginUserService",LoginUserService.class);
    }
		//压测其中一个dubbo服务的方法
    @Benchmark
    public void testStringAdd() {
        bizEnterpriseVipLogService.getInfoByOsn("VP201805242028553504");
    }

    @Benchmark
    public void testGetBaseUserInfo() {
        loginUserService.findBaseUserByUid(5963308);
    }


    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(JmhByUserService.class.getSimpleName())
                .output("/rgec/log/jmh.log") //将执行结果导入到文件中
                .build();
        new Runner(options).run();
    }

}
```

- 4.执行结果

```java
# JMH version: 1.21
# VM version: JDK 1.8.0_131, Java HotSpot(TM) 64-Bit Server VM, 25.131-b11
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java
# VM options: -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=50091:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8
# Warmup: 3 iterations, 10 s each
# Measurement: 10 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 8 threads, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: co.speedar.infra.JmhByUserService.testGetBaseUserInfo

# Run progress: 0.00% complete, ETA 00:04:20
# Fork: 1 of 1
objc[5822]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java (0x1072c64c0) and /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/libinstrument.dylib (0x10733c4e0). One of the two will be used. Which one is undefined.
# Warmup Iteration   1: 23.168 ±(99.9%) 0.499 ms/op
# Warmup Iteration   2: 30.436 ±(99.9%) 0.327 ms/op
# Warmup Iteration   3: 24.817 ±(99.9%) 0.252 ms/op
Iteration   1: 23.209 ±(99.9%) 0.136 ms/op
Iteration   2: 25.345 ±(99.9%) 0.202 ms/op
Iteration   3: 24.625 ±(99.9%) 0.117 ms/op
Iteration   4: 25.249 ±(99.9%) 0.262 ms/op
Iteration   5: 25.351 ±(99.9%) 0.234 ms/op
Iteration   6: 25.588 ±(99.9%) 0.091 ms/op
Iteration   7: 25.380 ±(99.9%) 0.189 ms/op
Iteration   8: 24.204 ±(99.9%) 0.230 ms/op
Iteration   9: 24.308 ±(99.9%) 0.145 ms/op
Iteration  10: 23.958 ±(99.9%) 0.142 ms/op


Result "co.speedar.infra.JmhByUserService.testGetBaseUserInfo":
  24.722 ±(99.9%) 1.189 ms/op [Average]
  (min, avg, max) = (23.209, 24.722, 25.588), stdev = 0.786
  CI (99.9%): [23.533, 25.911] (assumes normal distribution)


# JMH version: 1.21
# VM version: JDK 1.8.0_131, Java HotSpot(TM) 64-Bit Server VM, 25.131-b11
# VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java
# VM options: -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=50091:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8
# Warmup: 3 iterations, 10 s each
# Measurement: 10 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 8 threads, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: co.speedar.infra.JmhByUserService.testStringAdd

# Run progress: 50.00% complete, ETA 00:02:13
# Fork: 1 of 1
objc[5835]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/bin/java (0x1077e14c0) and /Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre/lib/libinstrument.dylib (0x108fff4e0). One of the two will be used. Which one is undefined.
# Warmup Iteration   1: 9.161 ±(99.9%) 0.145 ms/op
# Warmup Iteration   2: 7.669 ±(99.9%) 0.035 ms/op
# Warmup Iteration   3: 7.951 ±(99.9%) 0.034 ms/op
Iteration   1: 7.144 ±(99.9%) 0.024 ms/op
Iteration   2: 7.340 ±(99.9%) 0.060 ms/op
Iteration   3: 7.233 ±(99.9%) 0.027 ms/op
Iteration   4: 7.746 ±(99.9%) 0.063 ms/op
Iteration   5: 7.162 ±(99.9%) 0.029 ms/op
Iteration   6: 6.929 ±(99.9%) 0.023 ms/op
Iteration   7: 7.078 ±(99.9%) 0.042 ms/op
Iteration   8: 6.443 ±(99.9%) 0.039 ms/op
Iteration   9: 6.645 ±(99.9%) 0.019 ms/op
Iteration  10: 6.867 ±(99.9%) 0.021 ms/op


Result "co.speedar.infra.JmhByUserService.testStringAdd":
  7.059 ±(99.9%) 0.553 ms/op [Average]
  (min, avg, max) = (6.443, 7.059, 7.746), stdev = 0.366
  CI (99.9%): [6.506, 7.612] (assumes normal distribution)


# Run complete. Total time: 00:04:27

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                             Mode  Cnt   Score   Error  Units
JmhByUserService.testGetBaseUserInfo  avgt   10  24.722 ± 1.189  ms/op
JmhByUserService.testStringAdd        avgt   10   7.059 ± 0.553  ms/op

```

通过上面的最后执行结果统计可以看出testGetBaseUserInfo执行效率24毫秒，testStringAdd执行效率7毫秒

#### 参考文档
- 下面这篇文章中有每一个注解的含义，所以这里就不在重复说明了，大家可以直接去看看下面的文章
[Java微基准测试框架JMH](https://www.xncoding.com/2018/01/07/java/jmh.html)
- 下面是官方的一些demo，也可以供大家学习
[官方demo](<http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/>)


