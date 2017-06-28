package io.github.twktheainur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = "/", name = "restfulOntolex")
public class RestfulOntolex extends HttpServlet {

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final String uri2 = req.getRequestURI().substring(req.getContextPath().length());
        final String uri;
        if (uri2.startsWith("/")) {
            uri = uri2;
        } else {
            uri = "/" + uri2;
        }

        final ResultType mime;
        final String path;
        if (uri.matches(".*\\.html")) {
            mime = ResultType.html;
            path = uri.substring(0, uri.length() - 5);
        } else if (uri.matches(".*\\.rdf")) {
            mime = ResultType.rdfxml;
            path = uri.substring(0, uri.length() - 4);
        } else if (uri.matches(".*\\.ttl")) {
            mime = ResultType.turtle;
            path = uri.substring(0, uri.length() - 4);
        } else if (uri.matches(".*\\.nt")) {
            mime = ResultType.nt;
            path = uri.substring(0, uri.length() - 3);
        } else if (uri.matches(".*\\.json")) {
            mime = ResultType.jsonld;
            path = uri.substring(0, uri.length() - 5);
        } else {
            mime = bestMimeType(req.getHeader("Accept"), ResultType.html);
            path = uri;
        }

        String content = getContent(uri, mime);
        if (content == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);

        } else {
            res.addHeader("Vary", "Accept");
            res.addHeader("Content-Length", "" + content.length());
            res.addHeader("Content-Type", "todo");
            res.setStatus(HttpServletResponse.SC_OK);

            PrintWriter out = res.getWriter();
            out.println(content);
            out.flush();
            out.close();
        }

    }

    private ResultType bestMimeType(String acceptString, ResultType deflt) {
        String[] accepts = acceptString.split("\\s*,\\s*");
        for (String accept : accepts) {
            ResultType rt = mimeToResultType(accept, deflt);
            if (rt != null) {
                return rt;
            }
        }

        List<WeightedResultType> weightedAccepts = new ArrayList<WeightedResultType>();
        for (String accept : accepts) {
            if (accept.contains(";")) {
                try {
                    String[] e = accept.split("\\s*;\\s*");
                    ResultType mime = mimeToResultType(e[0], deflt);
                    for (int i = 1; i < e.length; i++) {
                        String extension = e[i];
                        if (extension.startsWith("q=") && mime != null) {
                            weightedAccepts.add(new WeightedResultType(Double.parseDouble(extension.substring(2)),
                                    mime));
                        }
                    }
                } catch (Exception x) {
                }
            }
        }
        if (weightedAccepts.isEmpty()) {
            return deflt;
        } else {
            weightedAccepts.sort(new Comparator<WeightedResultType>() {

                @Override
                public int compare(WeightedResultType o1, WeightedResultType o2) {
                    if (o1.weight > o2.weight) {
                        return -1;
                    } else if (o2.weight > o1.weight) {
                        return +1;
                    } else {
                        return o1.resultType.compareTo(o2.resultType);
                    }
                }
            });
            return weightedAccepts.get(0).resultType;
        }
    }

    private String getContent(String uri, ResultType mime) {
        return null;
    }

    private static class WeightedResultType {

        public double weight;
        public ResultType resultType;

        public WeightedResultType(double weight, ResultType resultType) {
            this.weight = weight;
            this.resultType = resultType;
        }
    }

    public ResultType mimeToResultType(String mime, ResultType deflt) {
        if (mime.equals("text/html")) {
            return ResultType.html;
        } else if (mime.equals("application/rdf+xml")) {
            return ResultType.rdfxml;
        } else if (mime.equals("text/turtle")) {
            return ResultType.turtle;
        } else if (mime.equals("application/x-turtle")) {
            return ResultType.turtle;
        } else if (mime.equals("application/n-triples")) {
            return ResultType.nt;
        } else if (mime.equals("text/plain")) {
            return ResultType.nt;
        } else if (mime.equals("application/ld+json")) {
            return ResultType.jsonld;
        } else if (mime.equals("application/json")) {
            return ResultType.jsonld;
        } else if (mime.equals("application/javascript")) {
            return ResultType.jsonld;
        } else {
            return null;
        }
    }
}
