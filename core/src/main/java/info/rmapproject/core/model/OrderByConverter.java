package info.rmapproject.core.model;

import info.rmapproject.core.model.request.OrderBy;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class OrderByConverter implements Converter<String, OrderBy> {

    @Override
    public OrderBy convert(String orderByString) {
        return OrderBy.getOrderByFromProperty(orderByString);
    }

}
