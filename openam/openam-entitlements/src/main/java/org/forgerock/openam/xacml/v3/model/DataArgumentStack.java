/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock Incorporated. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a Wrapper Class for Creating, Manipulating and traversing
 * a set of arguments which must be applied to a given Function.
 * <p/>
 *
 * X500Name
 * @see org.forgerock.openam.xacml.v3.Functions.AnyOfAny
 *
 */
public class DataArgumentStack implements Serializable {

    private DataValue argument;

    private List<DataValue> dataValues = new ArrayList<DataValue>();

    public DataArgumentStack() {
    }

    public DataArgumentStack(DataValue argument) {
        this.argument = argument;
    }

    public DataArgumentStack(DataValue argument, List<DataValue> dataValues) {
        this.argument = argument;
        this.dataValues = dataValues;
    }

    public DataValue getArgument() {
        return argument;
    }

    public void setArgument(DataValue argument) {
        this.argument = argument;
    }

    public List<DataValue> getDataValues() {
        return dataValues;
    }

    public void setDataValues(List<DataValue> dataValues) {
        this.dataValues = dataValues;
    }

    public boolean add(DataValue dataValue) {
        return this.dataValues.add(dataValue);
    }

    public boolean add(List<DataValue> dataValues) {
        return this.dataValues.addAll(dataValues);
    }

}
