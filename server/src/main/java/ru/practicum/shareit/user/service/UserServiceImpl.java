package ru.practicum.shareit.user.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.EmailException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUser;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUserDto;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto save(UserDto userDto) {

        try {
            return mapToUserDto(userRepository.save(mapToUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new EmailException("Пользователь с email: " + userDto.getEmail() + " уже существует.");
            }
        }
        return null;
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь с  ID #" + userId + " не существует.");
        });
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());

        try {
            return mapToUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new EmailException("Пользователь с email: " + userDto.getEmail() + " уже существует. ");
            }
        }
        return null;
    }

    @Override
    public UserDto get(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь с ID #" + userId + " не существует.");
        });
        return mapToUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(toList());
    }
}