/*******************************************************************************
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This software was produced as part of the RMap Project (http://rmap-project.info),
 * The RMap Project was funded by the Alfred P. Sloan Foundation and is a 
 * collaboration between Data Conservancy, Portico, and IEEE.
 *******************************************************************************/

package info.rmapproject.core.model.impl.openrdf;

import info.rmapproject.core.exception.RMapException;
import info.rmapproject.core.model.event.RMapEventTargetType;
import info.rmapproject.core.model.event.RMapEventType;
import info.rmapproject.core.vocabulary.impl.openrdf.PROV;
import info.rmapproject.core.vocabulary.impl.openrdf.RMAP;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ORUtil {
    /**
     * Construct ORMapEvent object from OpenRdf Statements.
     *
     * @param eventStmts the set of statements that form the Event object
     * @return the RMap Event object
     * @throws RMapException the RMap exception
     */
    public static ORMapEvent createORMapEventFromStmts(Set<Statement> eventStmts) throws RMapException {
        //TODO:consider moving the majority of this logic into the respective model.event object
        //instead pass in Set<Statement> to RMapEvent class
        if (eventStmts==null || eventStmts.size()==0){
            throw new RMapException ("null or empty list of event statements");
        }
        Statement eventTypeStmt = null;
        Statement eventTargetTypeStmt = null;
        Statement associatedAgentStmt = null;
        Statement descriptionStmt = null;
        Statement associatedKeyStmt = null;
        Statement startTimeStmt = null;
        Statement endTimeStmt = null;
        IRI context = null;
        Statement typeStatement = null;
        // for create  and update events
        List<Statement> createdObjects = new ArrayList<Statement>();
        // for update events
        Statement sourceObjectStatement = null;
        Statement derivationStatement = null;
        Statement inactivatedObjectStatement = null;
        //For update events the do a replace
        Statement replacedObjectStatement = null;
        // for Tombstone events
        Statement tombstoned = null;
        // for Delete events
        List<Statement> deletedObjects = new ArrayList<Statement>();;
        ORMapEvent event = null;
        for (Statement stmt:eventStmts){
            if (context==null){
                context = (IRI) stmt.getContext();
            } else if (! (context.equals(stmt.getContext()))){
                throw new RMapException("Non-match of context in event named graph: "
                        + "Expected context: " + context.stringValue() +
                        "; actual context: " + stmt.getContext().stringValue());
            }
            IRI predicate = stmt.getPredicate();
            if (predicate.equals(RDF.TYPE)){
                typeStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.EVENTTYPE)){
                eventTypeStmt = stmt;
                continue;
            }
            if (predicate.equals(RMAP.TARGETTYPE)){
                eventTargetTypeStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.STARTEDATTIME)){
                startTimeStmt =stmt;
                continue;
            }
            if (predicate.equals(PROV.ENDEDATTIME)){
                endTimeStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.WASASSOCIATEDWITH)){
                associatedAgentStmt = stmt;
                continue;
            }
            if (predicate.equals(DC.DESCRIPTION)){
                descriptionStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.USED)){
                associatedKeyStmt = stmt;
                continue;
            }
            if (predicate.equals(PROV.GENERATED)){
                createdObjects.add(stmt);
                continue;
            }
            if (predicate.equals(RMAP.HASSOURCEOBJECT)){
                sourceObjectStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.DERIVEDOBJECT)){
                derivationStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.INACTIVATEDOBJECT)){
                inactivatedObjectStatement = stmt;
                continue;
            }
            if (predicate.equals(RMAP.TOMBSTONEDOBJECT)){
                tombstoned = stmt;
                continue;
            }
            if (predicate.equals(RMAP.DELETEDOBJECT)){
                deletedObjects.add(stmt);
                continue;
            }
            if (predicate.equals(RMAP.UPDATEDOBJECT)){
                replacedObjectStatement=stmt;
                continue;
            }
        }
        // validate all required statements for all event types
        if (typeStatement != null){
            if (!(typeStatement.getObject().equals(RMAP.EVENT))){
                throw new RMapException("RDF type should be " + RMAP.EVENT.stringValue()
                        + " but is " + typeStatement.getObject().stringValue());
            }
        }
        boolean isCreateEvent = false;
        boolean isUpdateEvent = false;
        boolean isInactivateEvent = false;
        boolean isDerivationEvent = false;
        boolean isTombstoneEvent = false;
        boolean isDeleteEvent = false;
        boolean isReplaceEvent = false;
        if (eventTypeStmt==null){
            throw new RMapException ("No event type in event graph " + context.stringValue());
        }

        RMapEventType eventType = RMapEventType.getEventType(eventTypeStmt.getObject().stringValue());
        switch (eventType) {
            case CREATION : isCreateEvent = true;
                break;
            case UPDATE : isUpdateEvent = true;
                break;
            case INACTIVATION : isInactivateEvent = true;
                break;
            case DERIVATION : isDerivationEvent = true;
                break;
            case TOMBSTONE : isTombstoneEvent = true;
                break;
            case DELETION : isDeleteEvent = true;
                break;
            case REPLACE : isReplaceEvent = true;
                break;
            default :
                throw new RMapException ("Unrecognized event type: " + eventType
                    + " in event " + context.stringValue());
            }

        if (eventTargetTypeStmt==null){
            throw new RMapException("No event target type in event graph " + context.stringValue());
        }

        RMapEventTargetType eventTargetType = RMapEventTargetType.getEventTargetType(eventTargetTypeStmt.getObject().stringValue());

        switch(eventTargetType){
        case DISCO : break;
        case AGENT : break;
        default :
            throw new RMapException ("Unrecognized event target type: " + eventTargetType
                    + " in event " + context.stringValue());
        }

        if (associatedAgentStmt == null){
            throw new RMapException("No associated agent in event graph "
                    + context.stringValue());
        }
        if (startTimeStmt == null){
            throw new RMapException("No start time in event graph " + context.stringValue());
        }
        if (endTimeStmt == null){
            throw new RMapException("No end time in event graph " + context.stringValue());
        }
        // validate specific for each event type
        if (isCreateEvent){
            if (createdObjects.size()==0){
                throw new RMapException ("No new objects created in create event");
            }
            else {
                event = new ORMapEventCreation(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                        descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt,
                        createdObjects);
            }
        }
        else if (isUpdateEvent){
            if (inactivatedObjectStatement==null){
                throw new RMapException("Update event missing inactivated object statement");
            }
            if (derivationStatement == null ){
                throw new RMapException("Update event missing derived objec statement");
            }

            if (createdObjects.size()==0 ){
                throw new RMapException("Updated has no new created objects ");
            }
            event = new ORMapEventUpdate(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt,
                    createdObjects,derivationStatement,inactivatedObjectStatement);
        }
        else if (isInactivateEvent){
            if (inactivatedObjectStatement==null){
                throw new RMapException("Update event missing inactivated object statement");
            }
            event = new ORMapEventInactivation(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt, endTimeStmt, context, typeStatement, associatedKeyStmt,
                    inactivatedObjectStatement);
        }
        else if (isDerivationEvent){
            if (sourceObjectStatement==null){
                throw new RMapException("Update event missing source object statement");
            }
            if (derivationStatement == null ){
                throw new RMapException("Update event missing derived objec statement");
            }

            if (createdObjects.size()==0 ){
                throw new RMapException("Updated has no new created objects ");
            }
            event = new ORMapEventDerivation(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt,
                    createdObjects,derivationStatement,sourceObjectStatement);
        }
        else if (isTombstoneEvent){
            if (tombstoned==null){
                throw new RMapException("Tombstone event missing tombstoned object statement");
            }
            event = new ORMapEventTombstone(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt, tombstoned);
        }
        else if (isDeleteEvent){
            if(deletedObjects.size()==0){
                throw new RMapException ("Delete event has no deleted object ids");
            }
            event = new ORMapEventDeletion(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt,endTimeStmt, context, typeStatement, associatedKeyStmt, deletedObjects);
        }
        else if (isReplaceEvent){
            if (replacedObjectStatement==null){
                throw new RMapException("Update event missing replaced object statement");
            }
            event = new ORMapEventUpdateWithReplace(eventTypeStmt,eventTargetTypeStmt, associatedAgentStmt,
                    descriptionStmt, startTimeStmt, endTimeStmt, context, typeStatement, associatedKeyStmt,
                    replacedObjectStatement);
        }
        else {
            throw new RMapException ("Unrecognized event type");
        }
        return event;
    }
}
