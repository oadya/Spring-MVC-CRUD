package com.springmvc.crud.service;

import org.springframework.stereotype.Service;

import com.springmvc.crud.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserServiceImpl implements UserService {

    private static final AtomicInteger counter = new AtomicInteger();
    static List<User> users = new ArrayList<User>(
            Arrays.asList(
                    new User(counter.incrementAndGet(), "Daenerys Targaryen"),
                    new User(counter.incrementAndGet(), "John Snow"),
                    new User(counter.incrementAndGet(), "Arya Stark"),
                    new User(counter.incrementAndGet(), "Cersei Baratheon")));

    public List<User> getAll() {
        return users;
    }

    public User findById(int id) {
        for (User user : users){
            if (user.getId() == id){
                return user;
            }
        }
        return null;
    }

    public User findByName(String name) {
        for (User user : users){
            if (user.getUsername().equals(name)){
                return user;
            }
        }
        return null;
    }

    public void create(User user) {
        user.setId(counter.incrementAndGet());
        users.add(user);
    }

    public void update(User user) {
        int index = users.indexOf(user);
        users.set(index, user);
    }

    public void delete(int id) {
        User user = findById(id);
        users.remove(user);
    }

    public boolean exists(User user) {
        return findByName(user.getUsername()) != null;
    }
}