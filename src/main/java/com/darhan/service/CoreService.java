package com.darhan.service;

import com.darhan.entity.Result;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CoreService {
    private PythonInterpreter interpreter;
    private final ExecutorService executorService;

    public CoreService(int poolSize) {
        Properties props = new Properties();
        props.put("python.home", "path to the Lib folder");
        props.put("python.console.encoding", "UTF-8");
        props.put("python.security.respectJavaAccessibility", "false");
        props.put("python.import.site", "false");
        Properties preprops = System.getProperties();
        PythonInterpreter.initialize(preprops, props, new String[0]);
        this.interpreter = new PythonInterpreter();
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public Future<Result> setApiParams(String apiPath, Map input, JdbcTemplate jdbcTemplate, ServletRequest req, ServletResponse res) {
        return executorService.submit(() -> {
            this.interpreter.set("req",req);
            this.interpreter.execfile(apiPath);
            PyFunction handle = interpreter.get("handle", PyFunction.class);
            PyObject pyObj = Py.java2py(input);
            PyObject tempate = Py.java2py(jdbcTemplate);
            PyObject pyObject = handle.__call__(pyObj, tempate);
            Result result = (Result) pyObject.__tojava__(Result.class);
            interpreter.close();
            return result;
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
