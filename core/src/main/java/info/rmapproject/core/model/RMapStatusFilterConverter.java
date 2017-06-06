package info.rmapproject.core.model;

import info.rmapproject.core.model.request.RMapStatusFilter;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class RMapStatusFilterConverter implements Converter<String, RMapStatusFilter> {

    @Override
    public RMapStatusFilter convert(String statusFilter) {
        return RMapStatusFilter.getStatusFromTerm(statusFilter);
    }

}
