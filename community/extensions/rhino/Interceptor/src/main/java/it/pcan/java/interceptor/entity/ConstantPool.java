/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.entity;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Pierantonio
 */
public class ConstantPool extends ArrayList<AbstractConstant> {
    private static final long serialVersionUID = 42L;
    private final Map<Integer, AbstractConstant> map = new HashMap<Integer, AbstractConstant>();
    private final Map<String, Integer> utf8Map = new HashMap<String, Integer>();
    private int poolSize = 0;

    public Utf8Constant getUtf8(int index) throws InvalidClassException {
        AbstractConstant constant = map.get(index);
        if (!Utf8Constant.class.isAssignableFrom(constant.getClass())) {
            throw new InvalidClassException("Constant at index " + index + " is not an UTF8 string.");
        }
        return ((Utf8Constant) constant);
    }

    public int lookForUtf8Reference(String string) {
        Integer ret = utf8Map.get(string);
        if (ret == null) {
            return -1;
        }
        return ret.intValue();
    }

    public int lookForNameAndTypeReference(String name, String type) {
        int nameIndex = lookForUtf8Reference(name);
        int typeIndex = lookForUtf8Reference(type);
        return lookForNameAndTypeReference(nameIndex, typeIndex);
    }

    public int lookForNameAndTypeReference(int nameIndex, int typeIndex){
        if(nameIndex == -1 || typeIndex == -1) {
            return -1;
        }
        for(Entry<Integer, AbstractConstant> constant : map.entrySet()) {
            if(constant.getValue().getClass() == NameAndTypeConstant.class) {
                NameAndTypeConstant nameAndTypeConstant = (NameAndTypeConstant)(constant.getValue());
                if(nameAndTypeConstant.getNameIndex() == nameIndex && nameAndTypeConstant.getDescriptorIndex() == typeIndex) {
                    return constant.getKey().intValue();
                }
            }
        }
        return -1;
    }

    public int lookForClass(int nameIndex) {
        if (nameIndex == -1) {
            return -1;
        }
        for (Entry<Integer, AbstractConstant> constant : map.entrySet()) {
            if (constant.getValue().getClass() == ClassConstant.class) {
                ClassConstant classConstant = (ClassConstant)(constant.getValue());
                if (classConstant.getNameIndex() == nameIndex) {
                    return constant.getKey().intValue();
                }
            }
        }
        return -1;
    }

    public int lookForRef(int classIndex, int nameAndTypeIndex) {
        if(classIndex == -1 || nameAndTypeIndex == -1) {
            return -1;
        }
        for(Entry<Integer, AbstractConstant> constant : map.entrySet()) {
            if(RefConstant.class.isAssignableFrom(constant.getValue().getClass())) {
                RefConstant refConstant = (RefConstant)(constant.getValue());
                if(refConstant.getClassIndex() == classIndex && refConstant.getNameAndTypeIndex() == nameAndTypeIndex) {
                    return constant.getKey().intValue();
                }
            }
        }
        return -1;
    }

    public int lookForString(int utf8Index) {
        if(utf8Index == -1) {
            return -1;
        }
        for(Entry<Integer, AbstractConstant> constant : map.entrySet()) {
            if(StringConstant.class.isAssignableFrom(constant.getValue().getClass())) {
                StringConstant stringConstant = (StringConstant)(constant.getValue());
                if(stringConstant.getStringIndex() == utf8Index) {
                    return constant.getKey().intValue();
                }
            }
        }
        return -1;
    }

    public int lookForRef(int classIndex, int nameIndex, int typeIndex) {
        int nameAndTypeIndex = this.lookForNameAndTypeReference(nameIndex, typeIndex);
        return this.lookForRef(classIndex, nameAndTypeIndex);
    }

    @Override
    public boolean add(AbstractConstant e) {
        int index = poolSize + 1;
        map.put(index, e);
        if (e.getClass() == Utf8Constant.class) {
            utf8Map.put(((Utf8Constant) e).getString(), index);
        }
        if (e.getClass() == LongConstant.class || e.getClass() == DoubleConstant.class) {
            poolSize++;
        }
        poolSize++;
        return super.add(e);
    }

    @Override
    public void add(int index, AbstractConstant element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends AbstractConstant> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends AbstractConstant> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    /**
     * use getById
     * @param index
     * @return
     * @deprecated
     */
    @Override
    @Deprecated
    public AbstractConstant get(int index) {
        return super.get(index);
    }

    public AbstractConstant getById(int id) {
        return map.get(id);
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractConstant remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractConstant set(int index, AbstractConstant element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public int size() {
        return super.size();
    }

    public int poolSize(){
        return poolSize;
    }

    @Override
    public void trimToSize() {
        throw new UnsupportedOperationException();
    }
}
