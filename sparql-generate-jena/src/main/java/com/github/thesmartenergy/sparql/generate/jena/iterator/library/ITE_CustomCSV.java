/*
 * Copyright 2016 ITEA 12004 SEAS Project.
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
 */
package com.github.thesmartenergy.sparql.generate.jena.iterator.library;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.github.thesmartenergy.sparql.generate.jena.iterator.IteratorFunctionBase1;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import com.github.thesmartenergy.sparql.generate.jena.iterator.IteratorFunctionBase2;
import com.github.thesmartenergy.sparql.generate.jena.iterator.IteratorFunctionBase5;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDecimal;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.expr.nodevalue.NodeValueFloat;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import java.math.BigDecimal;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;
import org.supercsv.io.ICsvListReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * A SPARQL Iterator function that return a row of a CSV document, together with the header if it exists. The Iterator function URI is
 * {@code <http://w3id.org/sparql-generate/ite/CustomCSV>}. This iterator partly implements the CSV dialect description at 
 * @see <a href="https://www.w3.org/TR/tabular-metadata/#dialect-descriptions">CSVW Dialect Descriptions</a>
 * It takes five parameters as input:
 * <ul>
 *      <li>{@param csv} a RDF Literal with datatype URI 
 *      {@code <urn:iana:mime:text/csv>} representing the source CSV document</li>
 *      <li>{@param quoteChar} a RDF Literal with datatype {@code xsd:string} for the quote character</li>
 *      <li>{@param delimiterChar} a RDF Literal with datatype {@code xsd:string} for the delimiter character</li>
 *      <li>{@param endOfLineSymbols} a RDF Literal with datatype {@code xsd:string} for the end of line symbol</li>
 *      <li>{@param header} a RDF Literal with datatype {@code xsd:boolean} where true represents the presence of a header in the CSV document</li>
 * </ul>
 * and returns a RDF Literal with datatype URI
 * {@code <urn:iana:mime:text/csv>}.
 *
 * @author Noorani Bakerally
 */
public class ITE_CustomCSV extends IteratorFunctionBase5 {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(ITE_CustomCSV.class);

    /**
     * The SPARQL function URI.
     */
    public static final String URI = SPARQLGenerate.ITE + "CustomCSV";

    /**
     * The datatype URI of the first parameter and the return literals.
     */
    private static final String datatypeUri = "urn:iana:mime:text/csv";

    /**
     * {@inheritDoc }
     */
    @Override
    public List<NodeValue> exec(NodeValue csv,NodeValue quoteChar,NodeValue delimiterChar, NodeValue endOfLineSymbols,NodeValue header) {
         
        if (csv.getDatatypeURI() == null
                && datatypeUri == null
                || csv.getDatatypeURI() != null
                && !csv.getDatatypeURI().equals(datatypeUri)
                && !csv.getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#string")) {
            LOG.warn("The URI of NodeValue1 MUST be"
                    + " <" + datatypeUri + "> or"
                    + " <http://www.w3.org/2001/XMLSchema#string>. Got <"
                    + csv.getDatatypeURI() + ">. Returning null.");
        }
        
        try {
           
            String sourceCSV = String.valueOf(csv.asNode().getLiteralValue());
           
            ICsvListReader listReader = null;
            InputStream is = new ByteArrayInputStream(sourceCSV.getBytes());
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);
            
            
            String headerRow = "";
            System.out.println("test=================header"+header.getBoolean());
            if (header.getBoolean()){
                headerRow = br.readLine().split(endOfLineSymbols.asString())[0];
            }
            System.out.println("test=================header"+headerRow);
            
            //new CsvPreference.Builder('"', '\u0001', "\r\n").build()
            CsvPreference prefs = new CsvPreference.Builder(quoteChar.asString().charAt(0),delimiterChar.asString().charAt(0),
                    endOfLineSymbols.asString()).build();
            
            listReader = new CsvListReader(br, prefs);
           
            List<NodeValue> nodeValues = new ArrayList<>(listReader.length());
          
           
            while (listReader.read() != null){
                StringWriter sw = new StringWriter();
                
                CsvListWriter listWriter = new CsvListWriter(sw, CsvPreference.TAB_PREFERENCE);
                
                
                if (header.getBoolean()){
                     listWriter.writeHeader(headerRow);
                }
                
                listWriter.write(listReader.getUntokenizedRow());
                listWriter.close();
                
                NodeValue nodeValue = new NodeValueString(sw.toString());
                nodeValues.add(nodeValue);
            }
      
            return nodeValues;  
        } catch (Exception e) {
            throw new ExprEvalException("FunctionBase: no evaluation", e);
        }
    }
}