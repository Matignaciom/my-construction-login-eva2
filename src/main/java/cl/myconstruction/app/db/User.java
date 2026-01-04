package cl.myconstruction.app.db;

public record User(int id, String email, byte[] passwordHash, byte[] passwordSalt) {}

