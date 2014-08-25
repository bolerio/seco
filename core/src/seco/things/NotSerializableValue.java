package seco.things;

/**
 * <p>
 * This is a special purpose atom that holds a value at runtime that cannot
 * be stored in the database. Technically anything can be "forced" into serialization,
 * but that would be asking for too much trouble. Be it only because certain
 * results in a computation are not needed or can be easily recomputed so there is
 * no point in making the state complete in that sense.
 * </p> 
 * 
 * <p>
 * What prompted the creation of this class is a JTable model created
 * dynamically with BeanShell and that does not implement serializable, so 
 * writing the JTable Swing component to the DB fails. Since the question of
 * what should get persisted and what should get recomputed and how the dependencies
 * are really defined/inferred remains open, we stay in ad hoc land for now. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class NotSerializableValue
{
    private transient Object value;
    private transient boolean initialized = false;
    
    public NotSerializableValue() { }
    public NotSerializableValue(Object value) { this.value = value; this.initialized = true; }
    public NotSerializableValue value(Object value)
    {
        this.value = value;
        this.initialized = true;
        return this;
    }
    
    public Object value() 
    {
        return this.value;
    }
    
    public boolean initialized()
    {
        return this.initialized;
    }
}