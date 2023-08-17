package vertx.rinha;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class Database {

    private static final LinkedHashMap<String, Pessoa> pessoas = new LinkedHashMap<>();

    public static LinkedHashMap<String, Pessoa> getPessoas() {
        return pessoas;
    }

    public Pessoa save(Pessoa pessoa) {
        pessoa.setId(UUID.randomUUID().toString());
        pessoas.put(pessoa.getId(), pessoa);
        return pessoa;
    }

    public int count() {
        return pessoas.size();
    }

    public Pessoa findById(String id) {
        return pessoas.get(id);
    }

    public List<Pessoa> find(String search) {
        return pessoas.values().stream().filter(p -> matches(p, search)).limit(50).collect(java.util.stream.Collectors.toList());
    }

    private boolean matches(Pessoa pessoa, String search) {
        if (search == null || search.isEmpty()) {
            return true;
        }
        if (pessoa.getApelido().contains(search)) {
            return true;
        }
        if (pessoa.getNome().contains(search)) {
            return true;
        }
        if (pessoa.getStack() != null) {
            for (String s : pessoa.getStack()) {
                if (s != null && s.contains(search)) {
                    return true;
                }
            }
        }
        return false;
    }

}
