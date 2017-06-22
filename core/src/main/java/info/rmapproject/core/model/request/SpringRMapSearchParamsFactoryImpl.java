package info.rmapproject.core.model.request;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Component
public class SpringRMapSearchParamsFactoryImpl implements RMapSearchParamsFactory, ApplicationContextAware {

    private ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = applicationContext;
    }

    @Override
    public RMapSearchParams newInstance() {
        return appCtx.getBean(RMapSearchParams.class);
    }

}
