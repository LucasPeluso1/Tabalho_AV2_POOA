package edu.ucsal.fiadopay.service;

import edu.ucsal.fiadopay.annotation.AntiFraud;
import edu.ucsal.fiadopay.annotation.PaymentMethod;
import edu.ucsal.fiadopay.annotation.WebhookSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class AnnotationInspectorService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationInspectorService.class);

    private final ApplicationContext context;

    public AnnotationInspectorService(ApplicationContext context) {
        this.context = context;
    }


    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("--- Iniciando Inspeção de Anotações (Spring Context Scan) ---");


        Map<String, Object> beans = context.getBeansOfType(Object.class);

        for (Object bean : beans.values()) {
        
            Class<?> beanClass = AopUtils.getTargetClass(bean);

            if (beanClass.getPackageName() == null || !beanClass.getPackageName().startsWith("edu.ucsal.fiadopay")) {
                continue;
            }

            inspectClass(beanClass);
        }
        
        log.info("--- Inspeção de Anotações Concluída ---");
    }

    private void inspectClass(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PaymentMethod.class)) {
                PaymentMethod ann = method.getAnnotation(PaymentMethod.class);
                log.info("[REFLEXÃO] Método '{}#{}' é um @PaymentMethod: {}", clazz.getSimpleName(), method.getName(), ann.type());
            }
            if (method.isAnnotationPresent(AntiFraud.class)) {
                AntiFraud ann = method.getAnnotation(AntiFraud.class);
                log.info("[REFLEXÃO] Método '{}#{}' possui @AntiFraud: Nome={}, Threshold=R${}",
                        clazz.getSimpleName(), method.getName(), ann.name(), ann.threshold());
            }
            if (method.isAnnotationPresent(WebhookSink.class)) {
                log.info("[REFLEXÃO] Método '{}#{}' é um @WebhookSink.", clazz.getSimpleName(), method.getName());
            }
        }
    }
}