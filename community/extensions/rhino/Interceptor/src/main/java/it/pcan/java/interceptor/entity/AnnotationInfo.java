/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.entity;

/**
 * to be deleted. Useless.
 * @author Pierantonio
 * @deprecated
 */

@Deprecated
public class AnnotationInfo {

    private Class interceptorClass;

    private int typeIndex;
    private int numElements;

    public Class getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(Class interceptorClass) {
        this.interceptorClass = interceptorClass;
    }
    //private Element[] elements;

    

    public int getNumElements() {
        return numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

//
//    public Element[] getElements() {
//        return elements;
//    }
//
//    public void setElements(Element[] elements) {
//        this.elements = elements;
//    }
//

//    public class Element {
//
//        private int nameIndex;
//        private ElementValue value;
//
//        public int getNameIndex() {
//            return nameIndex;
//        }
//
//        public void setNameIndex(int nameIndex) {
//            this.nameIndex = nameIndex;
//        }
//
//        public ElementValue getValue() {
//            return value;
//        }
//
//        public void setValue(ElementValue value) {
//            this.value = value;
//        }
//
//        public class ElementValue {
//
//            private int tag;
//            //union:
//            private int constValueIndex;
//            private EnumConstValue enumConstValue;
//            private int classInfoIndex;
//            private AnnotationInfo annotationInfo;
//
//            public AnnotationInfo getAnnotationInfo() {
//                return annotationInfo;
//            }
//
//            public void setAnnotationInfo(AnnotationInfo annotationInfo) {
//                this.annotationInfo = annotationInfo;
//            }
//
//            public int getClassInfoIndex() {
//                return classInfoIndex;
//            }
//
//            public void setClassInfoIndex(int classInfoIndex) {
//                this.classInfoIndex = classInfoIndex;
//            }
//
//            public int getConstValueIndex() {
//                return constValueIndex;
//            }
//
//            public void setConstValueIndex(int constValueIndex) {
//                this.constValueIndex = constValueIndex;
//            }
//
//            public EnumConstValue getEnumConstValue() {
//                return enumConstValue;
//            }
//
//            public void setEnumConstValue(EnumConstValue enumConstValue) {
//                this.enumConstValue = enumConstValue;
//            }
//
//            public int getTag() {
//                return tag;
//            }
//
//            public void setTag(int tag) {
//                this.tag = tag;
//            }
//
//            public class EnumConstValue {
//
//                private int typeNameIndex;
//                private int constNameIndex;
//
//                public int getConstNameIndex() {
//                    return constNameIndex;
//                }
//
//                public void setConstNameIndex(int constNameIndex) {
//                    this.constNameIndex = constNameIndex;
//                }
//
//                public int getTypeNameIndex() {
//                    return typeNameIndex;
//                }
//
//                public void setTypeNameIndex(int typeNameIndex) {
//                    this.typeNameIndex = typeNameIndex;
//                }
//            }
//
//            public class ArrayValue {
//
//                private int numValues;
//                private ElementValue[] values;
//
//                public int getNumValues() {
//                    return numValues;
//                }
//
//                public void setNumValues(int numValues) {
//                    this.numValues = numValues;
//                }
//
//                public ElementValue[] getValues() {
//                    return values;
//                }
//
//                public void setValues(ElementValue[] values) {
//                    this.values = values;
//                }
//            }
//        }
//    }
}
