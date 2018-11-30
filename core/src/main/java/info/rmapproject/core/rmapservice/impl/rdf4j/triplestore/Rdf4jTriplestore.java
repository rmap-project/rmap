/*******************************************************************************
 * Copyright 2018 Johns Hopkins University
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
package info.rmapproject.core.rmapservice.impl.rdf4j.triplestore;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * The abstract class for  RDF4J Triplestore
 *
 * @author khanson, smorrissey
 */
public abstract class Rdf4jTriplestore  {

    /** true if the triplestore connection is open. */
    protected boolean connectionOpen = false;

    /** true if the triplestore transaction is open. */
    protected boolean transactionOpen = false;
        
    /** An instance of the RDF4J repository. */
    protected static Repository repository = null;

    /** An instance of the RDF4J connection. */
    protected RepositoryConnection connection = null;

    /** An instance of the RDF4J Value Factory for instantiating RDF4J types (e.g. BNode). */
    protected ValueFactory valueFactory = null;
	
	/**
	 * Instantiates a new RDF4J triplestore.
	 */
	protected Rdf4jTriplestore()	{}

	/**
	 * Gets the respository instance. Initiates it if it's not already initialized.
	 *
	 * @return the RDF4J repository instance
	 * @throws RepositoryException the repository exception
	 */
	protected Repository getRepository() throws RepositoryException {
		if (repository==null){
			intitializeRepository();
		}
		return repository;		
	}
	
	/**
	 * Gets the repository connection instance. Opens connection if not already open
	 *
	 * @return the connection
	 * @throws RepositoryException the repository exception
	 */
	public RepositoryConnection getConnection()	throws RepositoryException {
		if (connection==null || !hasConnectionOpen()){
			openConnection();
		}
		return connection;		
	}
	
	/**
	 * Opens the repository connection.
	 *
	 * @throws RepositoryException the repository exception
	 */
	public void openConnection() throws RepositoryException {
    	if (repository == null)	{
    		intitializeRepository();
    	}    	    	
    	if (connection == null || !hasConnectionOpen()) {
    		connection = repository.getConnection();
    	}	    
		setConnectionOpen(true);
	}
	
	/**
	 * Closes the repository connection
	 *
	 * @throws RepositoryException the repository exception
	 */
	public void closeConnection() throws RepositoryException {
		if (connection != null)	{
			connection.close(); 
			setConnectionOpen(false);
			}
	}
	
	/**
	 * Begins a transaction
	 *
	 * @throws RepositoryException the repository exception
	 */
	public void beginTransaction() throws RepositoryException {
		getConnection().begin();
		setTransactionOpen(true);
	}
	
	/**
	 * Rolls back transaction.
	 *
	 * @throws RepositoryException the repository exception
	 */
	public void rollbackTransaction() throws RepositoryException{
		if (hasTransactionOpen()){
			getConnection().rollback();
		}
		setTransactionOpen(false);
	}
	
	/**
	 * Commits a transaction.
	 *
	 * @throws RepositoryException the repository exception
	 */
	public void commitTransaction() throws RepositoryException{
		if (hasTransactionOpen()){
			getConnection().commit();
		}
		setTransactionOpen(false);
	}

	/**
	 * Adds a Statement to the triplestore.
	 *
	 * @param stmt the stmt
	 * @throws RepositoryException the repository exception
	 */
	public void addStatement(Statement stmt) throws RepositoryException {
		getConnection().add(stmt);
		return;
	}
	
	/**
	 * Adds a Statement to the triplestore based on a subject, object, and predicate.
	 *
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @throws RepositoryException the repository exception
	 */
	public void addStatement(Resource subj, IRI pred, Value obj) throws RepositoryException	{
		getConnection().add(subj,pred,obj);
		return;
	}
	
	/**
	 * Adds a Statement to the triplestore based on a subject, object, predicate, and context.
	 *
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @param context the statement context
	 * @throws RepositoryException the repository exception
	 */
	public void addStatement(Resource subj, IRI pred, Value obj, Resource context) throws RepositoryException	{
		getConnection().add(subj,pred,obj,context);
		return;
	}
	
	/**
	 * Retrieves a Set of statements matching a subject, predicate, and object.
	 * Nulls can be used as wildcards for s, p, and o.
	 *
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @return a set of statements matching the s,p,o provided
	 * @throws RepositoryException the repository exception
	 */
	public Set <Statement> getStatements(Resource subj, IRI pred, Value obj) throws RepositoryException {
		return getStatements(subj, pred, obj, false, null);
	}
	
	/**
	 * Retrieves a Set of statements matching a subject, predicate, object, and context.
	 * Nulls can be used as wildcards for s, p, o, and c.
	 * 
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @param includeInferred the include inferred statements (may not be supported depending on db platform)
	 * @param context the context
	 * @return set of Statements matching s, p, o, c provided
	 * @throws RepositoryException the repository exception
	 */
	public Set<Statement> getStatements(Resource subj, IRI pred, Value obj, boolean includeInferred, 
			Resource context) throws RepositoryException {
		RepositoryResult<Statement> resultset = null;
		Set <Statement> stmts = new HashSet <Statement>();
		if (context==null)	{
			resultset = getConnection().getStatements(subj, pred, obj, includeInferred);
		}
		else	{
		    if (resourceIsContext(context)) {
		        resultset = getConnection().getStatements(subj, pred, obj, includeInferred, context);
		    }
		}
		if (resultset!=null) {
    		while (resultset.hasNext()) {
    			Statement stmt = resultset.next();
    			stmts.add(stmt);
    		}	
		}
		return stmts;
	}
	
	/**
	 * Retrieves a Set of statements matching a subject, predicate, object, and context.
	 * Nulls can be used as wildcards for s, p, o, and c.
	 * 
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @param context the statement context
	 * @return a set of Statements matching the s,p,o,c provided
	 * @throws RepositoryException the repository exception
	 */
	public Set<Statement> getStatements(Resource subj, IRI pred, Value obj,Resource context) throws RepositoryException{
		return this.getStatements(subj, pred, obj, false, context);
	}
	
	/**
	 * Retrieves a Set of statements matching a subject, predicate, object.
	 * Nulls can be used as wildcards for s, p, o.
	 * 
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @param includeInferred the include inferred statements (may not be supported depending on db platform)
	 * @return list of statements matching s,p,o provided
	 * @throws Exception the exception
	 */
	public List<Statement> getStatementsAnyContext(Resource subj, IRI pred, Value obj, boolean includeInferred) 
			throws Exception {
		RepositoryResult<Statement> resultset = null;
		List <Statement> stmts = new ArrayList <Statement>();
		resultset = getConnection().getStatements(subj, pred, obj, includeInferred);
		while (resultset.hasNext()) {
		Statement stmt = resultset.next();
		stmts.add(stmt);
		}		
		return stmts;
	}
	
	/**
	 * Retrieves a statement matching a subject, predicate, object.
	 *
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @return retrieves a single statement matching the s, p, o provided
	 * @throws RepositoryException the repository exception
	 */
	//TODO  does this make sense?  you are looking for a single statement
	public Statement getStatementAnyContext (Resource subj, IRI pred, Value obj) throws RepositoryException {
		RepositoryResult<Statement> resultset = null;
		Statement stmt = null;
		resultset = getConnection().getStatements(subj, pred, obj, false);// might eventually want true here, or option for default
		if (resultset.hasNext()) {
			stmt = resultset.next();
		}		
		return stmt;
	}

	/**
	 * Retrieves a statement matching a subject, predicate, object, and context
	 *
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @param context the statement context
	 * @return retrieves a single statement matching the s, p, o, c provided
	 * @throws RepositoryException the repository exception
	 */
	//TODO  does this make sense?  you are looking for a single statement - what if wildcards used?
	public Statement getStatement(Resource subj, IRI pred, Value obj, Resource context) throws RepositoryException {
		RepositoryResult<Statement> resultset = null;
		Statement stmt = null;
		resultset = getConnection().getStatements(subj, pred, obj, false, context);// I think we want true here
		if (resultset.hasNext()) {
			stmt = resultset.next();
		}		
		return stmt;
	}
	
	/**
	 * Retrieves a statement matching a subject, predicate, object, and context
	 *
	 * @param subj the statement subject
	 * @param pred the statement predicate
	 * @param obj the statement object
	 * @return retrieves a single statement matching the s, p, o provided
	 * @throws RepositoryException the repository exception
	 */
	//TODO  does this make sense?  you are looking for a single statement
	public Statement getStatement(Resource subj, IRI pred, Value obj) throws RepositoryException {
		return getStatement(subj,pred,obj,null);
	}
	
	/**
	 * Executes SPARQL query against triplestore.  Note, the query must return a list containing spoc in order to convert 
	 * the results to a list of statements.
	 *
	 * @param sparqlQuery the SPARQL query
	 * @return list of statements returned by SPARQL query
	 * @throws Exception the exception
	 */
	public List<Statement> getStatementListBySPARQL(String sparqlQuery) 
			throws Exception {
		List <Statement> stmts = new ArrayList <Statement>();
		TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
		TupleQueryResult resultset = tupleQuery.evaluate();
		while (resultset.hasNext()) {
			BindingSet bindingSet = resultset.next();
			Statement stmt = getValueFactory().createStatement((Resource) bindingSet.getBinding("s").getValue(),
												(IRI)bindingSet.getBinding("p").getValue(),
												bindingSet.getBinding("o").getValue(),
												(Resource) bindingSet.getBinding("c").getValue());
			stmts.add(stmt);
			
		}		
		return stmts;
	}
	
	/**
	 * Executes SPARQL query against triplestore and returns one column list of URIs .
	 *
	 * @param sparqlQuery the SPARQL query
	 * @return list of values returned by SPARQL query
	 * @throws Exception the exception
	 */
	public List<BindingSet> getSPARQLQueryResults(String sparqlQuery)
			throws Exception {
		
		TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
		TupleQueryResult resultset = tupleQuery.evaluate();
		List<BindingSet> bs = QueryResults.stream(resultset).collect(Collectors.toList());
		return bs;
	}
	
	/**
	 * Checks if there is at least one statement with the matching context. If there is return true, 
	 * if not returns false.
	 * @param id
	 * @return true if there is at least one statement with this context, false if there is not
	 */
	public boolean resourceIsContext(Resource id) {
		String sparqlQuery = "select ?s ?p ?o where {GRAPH <" + id.toString() + "> {?s ?p ?o}} limit 1";
		TupleQuery tupleQuery = getConnection().prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
		TupleQueryResult resultset = tupleQuery.evaluate();
		if (resultset.hasNext()) {
			return true;
		}		
		return false;	
	}
		
	/**
	 * Removes a set of statements from the triplestore.
	 *
	 * @param stmts set of Statements to remove
	 * @param contexts the contexts of the Statements to remove
	 * @throws RepositoryException the repository exception
	 */
	public void removeStatements(Set<Statement> stmts, Resource...contexts) throws RepositoryException{
		this.getConnection().remove(stmts, contexts);
	}
	
	/**
	 * public BNode getBNode() throws Exception {
	 * 		// USE RMAP ids for all bnode ids
	 * 		String id = rmapIdService.createId().toASCIIString();
	 * 		BNode bnode = this.getValueFactory().createBNode(id);
	 * 		return bnode;
	 * 	}*
	 *
	 * @return the repository
	 * @throws RepositoryException the repository exception
	 */

	/**
	 * Abstract method to initialize a repository instance
	 * @return a repository instance.
	 * @throws RepositoryException
	 */
	protected abstract Repository intitializeRepository() throws RepositoryException;
	
	/**
	 * Retrieves a Value Factory instance, initializes one if it doesn't exist yet.
	 *
	 * @return the value factory
	 * @throws RepositoryException the repository exception
	 */
	private ValueFactory getValueFactory() throws RepositoryException{
		if (valueFactory==null){
			if (repository==null){
				this.intitializeRepository();
			}			
			valueFactory = repository.getValueFactory();
		}			
		return valueFactory;
	}
		
	/**
	 * Checks to see if the triplestore connection is open
	 *
	 * @return true, if connection is open.
	 */
	public boolean hasConnectionOpen()	{
		return (this.connectionOpen && connection!=null && connection.isOpen());
	}
		
	/**
	 * Checks for transaction open.
	 *
	 * @return true, if transaction is open
	 */
	public boolean hasTransactionOpen() {
		return this.transactionOpen;
	}

	/**
	 * Sets the connection status (true if open, false if closed)
	 *
	 * @param connOpen the new connection status. true=open; false=closed.
	 */
	protected void setConnectionOpen(Boolean connOpen)	{
		this.connectionOpen=connOpen;
	}

	/**
	 * Sets the transaction status (true if open, false if closed)
	 *
	 * @param transOpen the new transaction status. true=open; false=closed.
	 */
	protected void setTransactionOpen(Boolean transOpen)	{
		this.transactionOpen=transOpen;
	}
		  
}
