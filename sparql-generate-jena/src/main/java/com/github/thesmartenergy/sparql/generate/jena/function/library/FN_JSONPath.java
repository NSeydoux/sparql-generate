/*
 * Copyright 2016 Ecole des Mines de Saint-Etienne.
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
package com.github.thesmartenergy.sparql.generate.jena.function.library;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDecimal;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.expr.nodevalue.NodeValueFloat;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.apache.jena.assembler.JA.data;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;

/**
 * A SPARQL Function that extracts a string from a JSON document, according to a
 * JSONPath expression. The Function URI is
 * {@code <http://w3id.org/sparql-generate/fn/JSONPath>}.
 * 
 * @author Noorani Bakerally <noorani.bakerally at emse.fr>
 * @author Maxime Lefrançois <maxime.lefrancois at emse.fr>
 */
public final class FN_JSONPath extends FunctionBase2 {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(FN_JSONPath.class);

    /**
     * The SPARQL function URI.
     */
    public static final String URI = SPARQLGenerate.FN + "JSONPath";

    /**
     * The datatype URI of the first parameter and the return literals.
     */
    private static final String datatypeUri = "http://www.iana.org/assignments/media-types/application/json";

    /**
     *
     * @param json a RDF Literal with datatype URI
     * {@code <http://www.iana.org/assignments/media-types/application/json>} or {@code xsd:string}
     * @param jsonpath a RDF Literal with datatype {@code xsd:string}
     * @return a RDF Literal with datatype being the type of the object
     * extracted from the JSON document
     */
    @Override
    public NodeValue exec(NodeValue json, NodeValue jsonpath) {
        if (json.getDatatypeURI() == null
                && datatypeUri == null
                || json.getDatatypeURI() != null
                && !json.getDatatypeURI().equals(datatypeUri)
                && !json.getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#string")) {
            LOG.warn("The URI of NodeValue1 MUST be <" + datatypeUri + ">"
                    + "or <http://www.w3.org/2001/XMLSchema#string>."
                    + " Returning null.");
        }

        try {
            Object value = JsonPath.parse(json.asNode().getLiteralLexicalForm())
                    .limit(1).read(jsonpath.getString());

            if (value instanceof String) {
                return new NodeValueString((String) value);
            } else if (value instanceof Float) {
                return new NodeValueFloat((Float) value);
            } else if (value instanceof Boolean) {
                return new NodeValueBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                return new NodeValueInteger((Integer) value);
            } else if (value instanceof Long) {
                return new NodeValueInteger((Long) value);
            } else if (value instanceof Double) {
                return new NodeValueDouble((Double) value);
            } else if (value instanceof BigDecimal) {
                return new NodeValueDecimal((BigDecimal) value);
            } else if (value instanceof ArrayList) {
                String jsonString = new Gson().toJson(value);
                return new NodeValueString(jsonString);
            } else if (value instanceof Map) {
                String jsonString = new Gson().toJson(value, Map.class);
                return new NodeValueString(jsonString);
            } else {
                String strValue = String.valueOf(value);

                JsonParser parser = new JsonParser();
                JsonElement valElement = parser.parse(strValue);
                JsonArray list = valElement.getAsJsonArray();

                if (list.size() == 1) {
                    String jsonstring = list.get(0).getAsString();
                    Node node = NodeFactory.createLiteral(jsonstring);
                    NodeValue nodeValue = new NodeValueNode(node);
                    return nodeValue;

                } else {
                    return new NodeValueString(String.valueOf(value));
                }
            }
        } catch (Exception e) {
            throw new ExprEvalException("FunctionBase: no evaluation", e);
        }
    }
}
