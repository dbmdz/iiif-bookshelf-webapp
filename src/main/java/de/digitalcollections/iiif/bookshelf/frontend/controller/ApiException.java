package de.digitalcollections.iiif.bookshelf.frontend.controller;

import org.springframework.http.HttpStatus;

class ApiException extends RuntimeException {
  HttpStatus statusCode;
  String message;

  public ApiException(String message) {
    this(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public ApiException(String message, HttpStatus statusCode) {
    this.statusCode = statusCode;
    this.message = message;
  }
}
