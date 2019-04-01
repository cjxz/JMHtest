package co.speedar.infra;

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
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 10)//, time = 5, timeUnit = TimeUnit.SECONDS
@Threads(8)
@Fork(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class JmhByUserService {

    private BizEnterpriseVipLogService bizEnterpriseVipLogService;
    private LoginUserService loginUserService;

    @Setup
    public void init(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("application-dubbo.xml");
        bizEnterpriseVipLogService = ac.getBean("bizEnterpriseVipLogService",BizEnterpriseVipLogService.class);
        loginUserService = ac.getBean("loginUserService",LoginUserService.class);
    }

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
                .output("/rgec/log/jmh.log")
                .build();
        new Runner(options).run();
    }

}
