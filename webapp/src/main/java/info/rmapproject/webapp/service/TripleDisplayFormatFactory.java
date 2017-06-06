package info.rmapproject.webapp.service;

import info.rmapproject.core.model.RMapTriple;
import info.rmapproject.webapp.domain.TripleDisplayFormat;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public interface TripleDisplayFormatFactory {

    TripleDisplayFormat newTripleDisplayFormat();

    TripleDisplayFormat newTripleDisplayFormat(RMapTriple rmapTriple);

}
