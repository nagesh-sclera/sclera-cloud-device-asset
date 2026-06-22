package io.sclera.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for RemoteAccessProcessParser's pure parsing logic: buffering raw command
 * output, splitting docker vs host process lines, parsing port/host fields, and filtering
 * to the live processes. The process-execution paths are excluded (they shell out).
 */
class RemoteAccessProcessParserTest {

    @Test
    void fillData_appendsToBuffer() {
        RemoteAccessProcessParser parser = new RemoteAccessProcessParser();
        parser.fillData("abc");
        parser.fillData("def");
        assertEquals("abcdef", parser.data.toString());
    }

    @Test
    void formatData_parsesHostProcessAsAlive() {
        RemoteAccessProcessParser parser = new RemoteAccessProcessParser();
        parser.fillData("--local-port=8080 --remote-port=22 --remote-host=10.0.0.5");
        parser.formatData();

        List<ProcessData> alive = parser.getFinalProcessList();
        assertEquals(1, alive.size());
        ProcessData pd = alive.get(0);
        assertEquals(8080, pd.getLocalPort());
        assertEquals(22, pd.getRemotePort());
        assertEquals("10.0.0.5", pd.getRemoteHost());
        assertTrue(pd.isProcessAlive());
        assertFalse(pd.isDockerProcess());
    }

    @Test
    void formatData_dockerProcessIsNotAlive() {
        RemoteAccessProcessParser parser = new RemoteAccessProcessParser();
        parser.fillData("--local-port=9090 --remote-port=22 --remote-host=10.0.0.6 --isDockerProcess");
        parser.formatData();

        // docker process is parsed but marked not-alive, so it is filtered out of the live list
        assertTrue(parser.getFinalProcessList().isEmpty());
    }

    @Test
    void formatData_separatesDockerAndHostAcrossLines() {
        RemoteAccessProcessParser parser = new RemoteAccessProcessParser();
        parser.fillData(
                "--local-port=8080 --remote-port=22 --remote-host=10.0.0.5\n"
                        + "--local-port=9090 --remote-port=22 --remote-host=10.0.0.6 --isDockerProcess");
        parser.formatData();

        List<ProcessData> alive = parser.getFinalProcessList();
        // only the host process is alive
        assertEquals(1, alive.size());
        assertEquals(8080, alive.get(0).getLocalPort());
    }

    @Test
    void parseProcess_ignoresIncompleteLines() {
        RemoteAccessProcessParser parser = new RemoteAccessProcessParser();
        // missing remote-host -> ifEmpty() is true -> dropped
        parser.parseProcess(List.of("--local-port=8080 --remote-port=22"));
        assertTrue(parser.getFinalProcessList().isEmpty());
    }

    @Test
    void email_roundTrips() {
        RemoteAccessProcessParser parser = new RemoteAccessProcessParser();
        parser.setEmail("tech@example.com");
        assertEquals("tech@example.com", parser.getEmail());
    }
}
