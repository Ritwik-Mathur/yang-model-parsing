package com.yangparser.service;

import com.yangparser.model.YangModule;
import com.yangparser.parser.YangModelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class YangParserService {

    @Autowired
    private YangModelParser yangModelParser;

    /**
     * Parse YANG content from raw string.
     */
    public YangModule parseContent(String yangContent) {
        if (yangContent == null || yangContent.isBlank()) {
            throw new IllegalArgumentException("YANG content cannot be empty");
        }
        return yangModelParser.parse(yangContent);
    }

    /**
     * Parse YANG content from uploaded file.
     */
    public YangModule parseFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename != null && !filename.endsWith(".yang")) {
            throw new IllegalArgumentException("Only .yang files are supported");
        }
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        return parseContent(content);
    }

    /**
     * Returns a built-in sample YANG model for demo purposes.
     */
    public String getSampleYang() {
        return """
module network-device {
  yang-version 1.1;
  namespace "urn:example:network-device";
  prefix "nd";

  organization "Example Corp";
  contact "admin@example.com";
  description "A sample YANG model for a network device";

  revision 2024-01-15 {
    description "Initial version";
  }

  container system {
    description "System configuration";

    leaf hostname {
      type string {
        pattern "[a-zA-Z][a-zA-Z0-9\\-]*";
      }
      mandatory true;
      description "System hostname";
    }

    leaf domain-name {
      type string;
      description "DNS domain name";
    }

    container clock {
      description "System clock settings";
      leaf timezone {
        type string;
        default "UTC";
        description "Timezone string";
      }
    }
  }

  container interfaces {
    description "Network interfaces";

    list interface {
      key "name";
      description "List of network interfaces";

      leaf name {
        type string;
        mandatory true;
        description "Interface name e.g. eth0";
      }

      leaf enabled {
        type boolean;
        default "true";
        description "Whether the interface is enabled";
      }

      leaf mtu {
        type uint16 {
          range "68..65535";
        }
        default "1500";
        description "Maximum transmission unit";
      }

      container ipv4 {
        description "IPv4 address configuration";

        list address {
          key "ip";
          leaf ip {
            type string;
            mandatory true;
          }
          leaf prefix-length {
            type uint8 {
              range "0..32";
            }
            mandatory true;
          }
        }
      }

      container ipv6 {
        description "IPv6 address configuration";

        leaf enabled {
          type boolean;
          default "false";
        }
      }
    }
  }

  container routing {
    description "Routing configuration";

    list static-route {
      key "destination";
      description "Static routing table entries";

      leaf destination {
        type string;
        mandatory true;
        description "Destination network prefix";
      }

      leaf next-hop {
        type string;
        mandatory true;
        description "Next-hop IP address";
      }

      leaf metric {
        type uint32 {
          range "1..65535";
        }
        default "1";
        description "Route metric/preference";
      }
    }
  }
}
""";
    }
}
