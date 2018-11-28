package info.rmapproject.spring.jdbc.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Only initializes the database if the {@code DataSourceInitializer} {@link DataSourceInitializer#enabled is enabled}
 * and <em>any</em> {@link #getConditions() conditions} are {@code true}.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ConditionalDataSourceInitializer extends DataSourceInitializer {

    private static Logger LOG = LoggerFactory.getLogger(ConditionalDataSourceInitializer.class);

    private boolean enabled;

    private DataSource dataSource;

    private List<Function<DataSource, Boolean>> conditions = new ArrayList<>();

    public List<Function<DataSource, Boolean>> getConditions() {
        return conditions;
    }

    public void setConditions(List<Function<DataSource, Boolean>> conditions) {
        if (conditions == null || conditions.size() == 0) {
            throw new IllegalArgumentException("Conditions List must not be null or empty!");
        }
        this.conditions = conditions;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.enabled && anyConditionTrue(dataSource, conditions)) {
            super.afterPropertiesSet();
        }
    }

    private static boolean anyConditionTrue(DataSource dataSource, List<Function<DataSource, Boolean>> conditions) {
        final AtomicBoolean anyConditionHoldsTrue = new AtomicBoolean(Boolean.FALSE);
        final String msg = "Checking condition '%s': [%s]";
        conditions.forEach(condition -> {
            String conditionName = condition.getClass().getSimpleName();
            if (condition.apply(dataSource)) {
                LOG.debug(String.format(msg, conditionName, "true"));
                anyConditionHoldsTrue.set(true);
            } else {
                LOG.debug(String.format(msg, conditionName, "false"));
            }
        });

        if (anyConditionHoldsTrue.get()) {
            LOG.debug("At least one condition held true, database will be initialized.");
        }

        return anyConditionHoldsTrue.get();
    }
}
