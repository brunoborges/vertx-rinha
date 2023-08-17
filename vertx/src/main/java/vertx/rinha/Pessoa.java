package vertx.rinha;

import io.vertx.core.json.JsonObject;

public class Pessoa {

    String apelido;
    String nome;
    String nascimento;
    String[] stack;
    String id;

    public String getApelido() {
        return apelido;
    }

    public String getId() {
        return id;
    }

    public String getNascimento() {
        return nascimento;
    }

    public String getNome() {
        return nome;
    }

    public String[] getStack() {
        return stack;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setStack(String[] stack) {
        this.stack = stack;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof Pessoa) {
            return id.equals(((Pessoa) obj).id);
        }
        return false;
    }

    public void fromJsonObject(JsonObject jsonObject) {
        this.apelido = jsonObject.getString("apelido");
        this.nome = jsonObject.getString("nome");
        this.nascimento = jsonObject.getString("nascimento");
        this.stack = jsonObject.getJsonArray("stack").stream().toArray(String[]::new);
        this.id = jsonObject.getString("id");
    }

}
