package rt.tests.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import rt.tests.accumappender.Markers;
import rt.tests.annotation.ExtLogging;
import rt.tests.logger.ExtLogger;

@Aspect
public class ExtLoggingAspect {

    @Around("@annotation(annotation) && execution(* *(..))")
    public Object around(ProceedingJoinPoint pjp, ExtLogging annotation) throws Throwable {

        ExtLogger logger = ExtLogger.create(pjp.getTarget().getClass());

        try {
            Object result = pjp.proceed();

            logger.info(Markers.SUCCESS, "Completed successfully.");

            return result;

        } catch (Throwable t) {
            logger.error(Markers.FAIL, "Exception encountered: ", t);
            throw t;
        }
    }
}
