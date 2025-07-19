package apps.output;

public class LogEntry {
    public enum Type{
        Info,
        Packet,
        Warning,
        ErrorMessage
    }
    public LogEntry(String string, Type type){
        this.string = string;
        this.type = type;
    }
    public String string;
    public Type type;
}
