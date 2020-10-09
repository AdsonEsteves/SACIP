package sacip.sti.utils;

import java.util.List;
import java.util.Map;


public interface CypherExecutor {
    List<Map<String, Object>> readquery(String statement, Map<String,Object> params);

    List<Map<String, Object>> writequery(String statement, Map<String,Object> params);
}
