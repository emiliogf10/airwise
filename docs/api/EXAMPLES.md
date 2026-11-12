# Ejemplos sintéticos del contrato airwise-v1

Ejemplos documentales de AIR-012, sujetos a los contratos normativos de [API_DESIGN.md](API_DESIGN.md) y [NORMALIZED_CONTRACTS.md](../architecture/NORMALIZED_CONTRACTS.md). Los vuelos, aerolíneas D1/D2, importes, vendedores y estados son sintéticos. Los códigos de aeropuerto son referencias reales y su metadata se verificará antes de incorporar fixtures en AIR-013. Ninguna respuesta corresponde a un endpoint ya ejecutado.

Los calendarios y escenarios muestran contenido ilustrativo de demo-v1. Su forma y reglas son normativas. AIR-013 materializará el registro de fixtures de manera coherente y versionada.

## Catálogo

GET `/api/v1/demo/airports?datasetVersion=demo-v1&page=0&pageSize=20` devuelve este cuerpo 200. La cabecera X-Correlation-ID coincide con correlationId.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000001",
  "dataset": {
    "datasetVersion": "demo-v1",
    "evaluationInstant": "2027-01-01T00:00:00Z",
    "referenceDate": "2027-01-01",
    "calendarStart": "2027-01-01",
    "calendarEnd": "2027-12-31",
    "currencies": [
      {
        "code": "EUR",
        "minorUnits": 2
      },
      {
        "code": "USD",
        "minorUnits": 2
      }
    ],
    "airlineCodes": [
      "D1",
      "D2"
    ],
    "capabilities": {
      "providerId": "DEMO",
      "itineraries": "SUPPORTED",
      "priceObservations": "SUPPORTED",
      "oneWay": "SUPPORTED",
      "roundTrip": "SUPPORTED",
      "sellerDetails": "UNSUPPORTED",
      "baggageDetails": "SUPPORTED",
      "outputKinds": [
        "OFFER",
        "OBSERVATION"
      ],
      "maxOriginsPerCall": 1,
      "requiresNetwork": false,
      "requiresCredentials": false
    }
  },
  "items": [
    {
      "code": "BCN",
      "name": "Barcelona",
      "city": "Barcelona",
      "countryCode": "ES",
      "timeZone": "Europe/Madrid"
    },
    {
      "code": "MAD",
      "name": "Madrid",
      "city": "Madrid",
      "countryCode": "ES",
      "timeZone": "Europe/Madrid"
    },
    {
      "code": "OPO",
      "name": "Oporto",
      "city": "Oporto",
      "countryCode": "PT",
      "timeZone": "Europe/Lisbon"
    },
    {
      "code": "SCQ",
      "name": "Santiago de Compostela",
      "city": "Santiago de Compostela",
      "countryCode": "ES",
      "timeZone": "Europe/Madrid"
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "totalCount": 4,
    "hasMore": false
  }
}
```

## Búsqueda de ida

POST `/api/v1/demo/flight-searches`. Solicitud que muestra la normalización de códigos.

```json
{
  "query": {
    "tripType": "ONE_WAY",
    "origins": [
      " opo "
    ],
    "destination": "mad",
    "outboundDate": "2027-02-10",
    "returnDate": null,
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1",
  "filters": {},
  "sort": "DEFAULT",
  "page": 0,
  "pageSize": 20
}
```

## Respuesta de ida

HTTP 200 COMPLETE. Todos los horarios y precios pertenecen al escenario sintético.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000002",
  "context": {
    "datasetVersion": "demo-v1",
    "fingerprint": "q_f4cb1de712e0a33f1260c695fb0ccdf53d6f4da2ec05fa534625969aac8dd930",
    "query": {
      "tripType": "ONE_WAY",
      "origins": [
        "OPO"
      ],
      "destination": "MAD",
      "outboundDate": "2027-02-10",
      "returnDate": null,
      "travellers": {
        "adults": 1
      },
      "cabin": "ECONOMY"
    },
    "dataOrigin": "DEMO",
    "evaluationInstant": "2027-01-01T00:00:00Z"
  },
  "status": "COMPLETE",
  "offers": [
    {
      "offerId": "of_f04264b1cb41c28788bb5f40d3c18f96f88966050d68ffc6f3f3cc286afc3c83",
      "providerId": "DEMO",
      "dataOrigin": "DEMO",
      "itinerary": {
        "tripType": "ONE_WAY",
        "journeys": [
          {
            "direction": "OUTBOUND",
            "segments": [
              {
                "segmentId": "s-opo-outbound",
                "departureAirport": "OPO",
                "arrivalAirport": "MAD",
                "departureTime": {
                  "localDateTime": "2027-02-10T09:00:00",
                  "offset": "+00:00",
                  "zoneId": "Europe/Lisbon",
                  "instant": "2027-02-10T09:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-10T11:10:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-10T10:10:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D1",
                  "name": "Aerolínea Demo D1"
                },
                "operatingAirline": null,
                "flightNumber": "D1101",
                "technicalStops": 0,
                "durationSeconds": 4200
              }
            ],
            "connections": [],
            "durationSeconds": 4200
          }
        ],
        "durationSeconds": 4200,
        "connectionCount": 0
      },
      "price": {
        "money": {
          "amount": "100.00",
          "currency": "EUR"
        },
        "unknownReason": null,
        "evidenceKind": "RECENT_SEARCH",
        "scope": {
          "basis": "GROUP",
          "travellerCount": 1,
          "cabin": "ECONOMY",
          "coverage": "FULL_ITINERARY",
          "coveredSegmentIds": []
        },
        "taxes": "INCLUDED",
        "additionalCosts": [],
        "observedAt": "2026-12-31T23:55:00Z",
        "retrievedAt": "2027-01-01T00:00:00Z",
        "expiresAt": "2027-01-01T00:10:00Z",
        "freshness": "FRESH"
      },
      "seller": null,
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "comparisonGroupId": "g_35e6380641ec2860a4ddc4ff437324314a8d55316744695a07c26210f66ad190",
      "warnings": [
        {
          "code": "BAGGAGE_UNKNOWN",
          "path": "/baggage/checked",
          "message": "Equipaje facturado desconocido."
        }
      ]
    }
  ],
  "priceGroups": [
    {
      "groupId": "g_35e6380641ec2860a4ddc4ff437324314a8d55316744695a07c26210f66ad190",
      "dataOrigin": "DEMO",
      "evidenceKind": "RECENT_SEARCH",
      "currency": "EUR",
      "scope": {
        "basis": "GROUP",
        "travellerCount": 1,
        "cabin": "ECONOMY",
        "coverage": "FULL_ITINERARY",
        "coveredSegmentIds": []
      },
      "taxes": "INCLUDED",
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "additionalCostKinds": [],
      "monetarilyComparable": true,
      "reasons": []
    }
  ],
  "observations": [],
  "observationCount": 0,
  "observationsTruncated": false,
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "baseCount": 1,
    "filteredCount": 1,
    "excludedUnknownCount": 0,
    "hasMore": false
  },
  "providerResults": [
    {
      "providerId": "DEMO",
      "origin": "OPO",
      "status": "COMPLETE",
      "requiredUnits": 1,
      "completedUnits": 1,
      "offerCount": 1,
      "observationCount": 0,
      "issues": []
    }
  ],
  "warnings": [],
  "errors": []
}
```

## Búsqueda de ida y vuelta con dos orígenes

Solicitud POST. El orden canónico de los orígenes será OPO, SCQ.

```json
{
  "query": {
    "tripType": "ROUND_TRIP",
    "origins": [
      "SCQ",
      "OPO"
    ],
    "destination": "MAD",
    "outboundDate": "2027-02-10",
    "returnDate": "2027-02-17",
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1",
  "filters": {},
  "sort": "DEFAULT",
  "page": 0,
  "pageSize": 20
}
```

## Respuesta de ida y vuelta

HTTP 200 COMPLETE. Cada oferta contiene ambos trayectos. La observación conserva solo una tarifa de ida sin itinerario completo.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000003",
  "context": {
    "datasetVersion": "demo-v1",
    "fingerprint": "q_cdc1ab42a04b16b44746bf57d857b8db43675268614315a589876689f6fb5888",
    "query": {
      "tripType": "ROUND_TRIP",
      "origins": [
        "OPO",
        "SCQ"
      ],
      "destination": "MAD",
      "outboundDate": "2027-02-10",
      "returnDate": "2027-02-17",
      "travellers": {
        "adults": 1
      },
      "cabin": "ECONOMY"
    },
    "dataOrigin": "DEMO",
    "evaluationInstant": "2027-01-01T00:00:00Z"
  },
  "status": "COMPLETE",
  "offers": [
    {
      "offerId": "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378",
      "providerId": "DEMO",
      "dataOrigin": "DEMO",
      "itinerary": {
        "tripType": "ROUND_TRIP",
        "journeys": [
          {
            "direction": "OUTBOUND",
            "segments": [
              {
                "segmentId": "s-scq-outbound",
                "departureAirport": "SCQ",
                "arrivalAirport": "MAD",
                "departureTime": {
                  "localDateTime": "2027-02-10T10:00:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-10T09:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-10T11:00:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-10T10:00:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D2",
                  "name": "Aerolínea Demo D2"
                },
                "operatingAirline": null,
                "flightNumber": "D2101",
                "technicalStops": 0,
                "durationSeconds": 3600
              }
            ],
            "connections": [],
            "durationSeconds": 3600
          },
          {
            "direction": "INBOUND",
            "segments": [
              {
                "segmentId": "s-scq-inbound",
                "departureAirport": "MAD",
                "arrivalAirport": "SCQ",
                "departureTime": {
                  "localDateTime": "2027-02-17T18:00:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-17T17:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-17T19:00:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-17T18:00:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D2",
                  "name": "Aerolínea Demo D2"
                },
                "operatingAirline": null,
                "flightNumber": "D2101",
                "technicalStops": 0,
                "durationSeconds": 3600
              }
            ],
            "connections": [],
            "durationSeconds": 3600
          }
        ],
        "durationSeconds": 7200,
        "connectionCount": 0
      },
      "price": {
        "money": {
          "amount": "110.00",
          "currency": "EUR"
        },
        "unknownReason": null,
        "evidenceKind": "RECENT_SEARCH",
        "scope": {
          "basis": "GROUP",
          "travellerCount": 1,
          "cabin": "ECONOMY",
          "coverage": "FULL_ITINERARY",
          "coveredSegmentIds": []
        },
        "taxes": "INCLUDED",
        "additionalCosts": [],
        "observedAt": "2026-12-31T23:55:00Z",
        "retrievedAt": "2027-01-01T00:00:00Z",
        "expiresAt": "2027-01-01T00:10:00Z",
        "freshness": "FRESH"
      },
      "seller": null,
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "comparisonGroupId": "g_b289cc4fb8608d0a975df98d17df28d11ddd66727ff492846c5ac475ea8cc57c",
      "warnings": [
        {
          "code": "BAGGAGE_UNKNOWN",
          "path": "/baggage/checked",
          "message": "Equipaje facturado desconocido."
        }
      ]
    },
    {
      "offerId": "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
      "providerId": "DEMO",
      "dataOrigin": "DEMO",
      "itinerary": {
        "tripType": "ROUND_TRIP",
        "journeys": [
          {
            "direction": "OUTBOUND",
            "segments": [
              {
                "segmentId": "s-opo-outbound",
                "departureAirport": "OPO",
                "arrivalAirport": "MAD",
                "departureTime": {
                  "localDateTime": "2027-02-10T09:00:00",
                  "offset": "+00:00",
                  "zoneId": "Europe/Lisbon",
                  "instant": "2027-02-10T09:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-10T11:10:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-10T10:10:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D1",
                  "name": "Aerolínea Demo D1"
                },
                "operatingAirline": null,
                "flightNumber": "D1101",
                "technicalStops": 0,
                "durationSeconds": 4200
              }
            ],
            "connections": [],
            "durationSeconds": 4200
          },
          {
            "direction": "INBOUND",
            "segments": [
              {
                "segmentId": "s-opo-inbound",
                "departureAirport": "MAD",
                "arrivalAirport": "OPO",
                "departureTime": {
                  "localDateTime": "2027-02-17T18:00:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-17T17:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-17T18:10:00",
                  "offset": "+00:00",
                  "zoneId": "Europe/Lisbon",
                  "instant": "2027-02-17T18:10:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D1",
                  "name": "Aerolínea Demo D1"
                },
                "operatingAirline": null,
                "flightNumber": "D1101",
                "technicalStops": 0,
                "durationSeconds": 4200
              }
            ],
            "connections": [],
            "durationSeconds": 4200
          }
        ],
        "durationSeconds": 8400,
        "connectionCount": 0
      },
      "price": {
        "money": {
          "amount": "100.00",
          "currency": "EUR"
        },
        "unknownReason": null,
        "evidenceKind": "RECENT_SEARCH",
        "scope": {
          "basis": "GROUP",
          "travellerCount": 1,
          "cabin": "ECONOMY",
          "coverage": "FULL_ITINERARY",
          "coveredSegmentIds": []
        },
        "taxes": "INCLUDED",
        "additionalCosts": [],
        "observedAt": "2026-12-31T23:55:00Z",
        "retrievedAt": "2027-01-01T00:00:00Z",
        "expiresAt": "2027-01-01T00:10:00Z",
        "freshness": "FRESH"
      },
      "seller": null,
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "comparisonGroupId": "g_b289cc4fb8608d0a975df98d17df28d11ddd66727ff492846c5ac475ea8cc57c",
      "warnings": [
        {
          "code": "BAGGAGE_UNKNOWN",
          "path": "/baggage/checked",
          "message": "Equipaje facturado desconocido."
        }
      ]
    }
  ],
  "priceGroups": [
    {
      "groupId": "g_b289cc4fb8608d0a975df98d17df28d11ddd66727ff492846c5ac475ea8cc57c",
      "dataOrigin": "DEMO",
      "evidenceKind": "RECENT_SEARCH",
      "currency": "EUR",
      "scope": {
        "basis": "GROUP",
        "travellerCount": 1,
        "cabin": "ECONOMY",
        "coverage": "FULL_ITINERARY",
        "coveredSegmentIds": []
      },
      "taxes": "INCLUDED",
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "additionalCostKinds": [],
      "monetarilyComparable": true,
      "reasons": []
    }
  ],
  "observations": [
    {
      "observationId": "ob_b5c5fe1949d4ca0fecdb82c049777bb364c817f2ca42e9e9f1a1280f8bc24cfb",
      "providerId": "DEMO",
      "dataOrigin": "DEMO",
      "origin": "OPO",
      "destination": "MAD",
      "tripType": "ROUND_TRIP",
      "outboundDate": "2027-02-10",
      "returnDate": "2027-02-17",
      "price": {
        "money": {
          "amount": "45.00",
          "currency": "EUR"
        },
        "unknownReason": null,
        "evidenceKind": "OBSERVED_CACHE",
        "scope": {
          "basis": "GROUP",
          "travellerCount": 1,
          "cabin": "ECONOMY",
          "coverage": "OUTBOUND_ONLY",
          "coveredSegmentIds": []
        },
        "taxes": "INCLUDED",
        "additionalCosts": [],
        "observedAt": "2026-12-31T23:55:00Z",
        "retrievedAt": "2027-01-01T00:00:00Z",
        "expiresAt": "2027-01-01T00:10:00Z",
        "freshness": "FRESH"
      },
      "warnings": [
        {
          "code": "OBSERVATION_ONLY",
          "path": null,
          "message": "Observación sin itinerario completo."
        }
      ]
    }
  ],
  "observationCount": 1,
  "observationsTruncated": false,
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "baseCount": 2,
    "filteredCount": 2,
    "excludedUnknownCount": 0,
    "hasMore": false
  },
  "providerResults": [
    {
      "providerId": "DEMO",
      "origin": "OPO",
      "status": "COMPLETE",
      "requiredUnits": 1,
      "completedUnits": 1,
      "offerCount": 1,
      "observationCount": 1,
      "issues": []
    },
    {
      "providerId": "DEMO",
      "origin": "SCQ",
      "status": "COMPLETE",
      "requiredUnits": 1,
      "completedUnits": 1,
      "offerCount": 1,
      "observationCount": 0,
      "issues": []
    }
  ],
  "warnings": [],
  "errors": []
}
```

## Comparación con estimaciones

POST `/api/v1/demo/comparisons`. Se seleccionan las dos ofertas de la respuesta anterior y se declaran componentes terrestres de ida y regreso.

```json
{
  "query": {
    "tripType": "ROUND_TRIP",
    "origins": [
      "OPO",
      "SCQ"
    ],
    "destination": "MAD",
    "outboundDate": "2027-02-10",
    "returnDate": "2027-02-17",
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1",
  "offerIds": [
    "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
    "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378"
  ],
  "primary": "KNOWN_SUBTOTAL",
  "requiredComponents": [
    "ORIGIN_GROUND_OUTBOUND",
    "ORIGIN_GROUND_RETURN"
  ],
  "groundEstimates": [
    {
      "offerId": "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
      "component": "ORIGIN_GROUND_OUTBOUND",
      "money": {
        "amount": "30.00",
        "currency": "EUR"
      },
      "basis": "GROUP",
      "travellerCount": 1,
      "source": "USER_ESTIMATE",
      "assumption": "USER_PROVIDED"
    },
    {
      "offerId": "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
      "component": "ORIGIN_GROUND_RETURN",
      "money": {
        "amount": "30.00",
        "currency": "EUR"
      },
      "basis": "GROUP",
      "travellerCount": 1,
      "source": "USER_ESTIMATE",
      "assumption": "USER_PROVIDED"
    },
    {
      "offerId": "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378",
      "component": "ORIGIN_GROUND_OUTBOUND",
      "money": {
        "amount": "10.00",
        "currency": "EUR"
      },
      "basis": "GROUP",
      "travellerCount": 1,
      "source": "USER_ESTIMATE",
      "assumption": "USER_PROVIDED"
    },
    {
      "offerId": "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378",
      "component": "ORIGIN_GROUND_RETURN",
      "money": {
        "amount": "10.00",
        "currency": "EUR"
      },
      "basis": "GROUP",
      "travellerCount": 1,
      "source": "USER_ESTIMATE",
      "assumption": "USER_PROVIDED"
    }
  ]
}
```

## Respuesta de comparación

HTTP 200. Los subtotales seleccionados son 160 EUR para OPO y 130 EUR para SCQ. Las estimaciones cambian el orden respecto al importe base de vuelo.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000004",
  "context": {
    "datasetVersion": "demo-v1",
    "fingerprint": "q_cdc1ab42a04b16b44746bf57d857b8db43675268614315a589876689f6fb5888",
    "query": {
      "tripType": "ROUND_TRIP",
      "origins": [
        "OPO",
        "SCQ"
      ],
      "destination": "MAD",
      "outboundDate": "2027-02-10",
      "returnDate": "2027-02-17",
      "travellers": {
        "adults": 1
      },
      "cabin": "ECONOMY"
    },
    "dataOrigin": "DEMO",
    "evaluationInstant": "2027-01-01T00:00:00Z"
  },
  "primary": "KNOWN_SUBTOTAL",
  "requiredComponents": [
    "ORIGIN_GROUND_OUTBOUND",
    "ORIGIN_GROUND_RETURN"
  ],
  "groups": [
    {
      "groupId": "g_b289cc4fb8608d0a975df98d17df28d11ddd66727ff492846c5ac475ea8cc57c",
      "rows": [
        {
          "offerId": "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378",
          "rank": 1,
          "primaryKnown": true,
          "flightPrice": {
            "amount": "110.00",
            "currency": "EUR"
          },
          "durationSeconds": 7200,
          "connectionCount": 0,
          "knownSubtotal": {
            "amount": "130.00",
            "currency": "EUR"
          },
          "groundEstimates": [
            {
              "offerId": "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378",
              "component": "ORIGIN_GROUND_OUTBOUND",
              "money": {
                "amount": "10.00",
                "currency": "EUR"
              },
              "basis": "GROUP",
              "travellerCount": 1,
              "source": "USER_ESTIMATE",
              "assumption": "USER_PROVIDED"
            },
            {
              "offerId": "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378",
              "component": "ORIGIN_GROUND_RETURN",
              "money": {
                "amount": "10.00",
                "currency": "EUR"
              },
              "basis": "GROUP",
              "travellerCount": 1,
              "source": "USER_ESTIMATE",
              "assumption": "USER_PROVIDED"
            }
          ],
          "missingFields": [],
          "reasons": []
        },
        {
          "offerId": "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
          "rank": 2,
          "primaryKnown": true,
          "flightPrice": {
            "amount": "100.00",
            "currency": "EUR"
          },
          "durationSeconds": 8400,
          "connectionCount": 0,
          "knownSubtotal": {
            "amount": "160.00",
            "currency": "EUR"
          },
          "groundEstimates": [
            {
              "offerId": "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
              "component": "ORIGIN_GROUND_OUTBOUND",
              "money": {
                "amount": "30.00",
                "currency": "EUR"
              },
              "basis": "GROUP",
              "travellerCount": 1,
              "source": "USER_ESTIMATE",
              "assumption": "USER_PROVIDED"
            },
            {
              "offerId": "of_d76f2960d8421d80e8407e0484e0aae160d30b4fb644a688b3f204da577b08df",
              "component": "ORIGIN_GROUND_RETURN",
              "money": {
                "amount": "30.00",
                "currency": "EUR"
              },
              "basis": "GROUP",
              "travellerCount": 1,
              "source": "USER_ESTIMATE",
              "assumption": "USER_PROVIDED"
            }
          ],
          "missingFields": [],
          "reasons": []
        }
      ],
      "rankingComplete": true,
      "recommendationIds": [
        "of_6243f8bd1c7b49e805e68b4bfb78b1e9407a6e1331d24c52c21163c0b7b45378"
      ],
      "recommendationScope": "EVALUABLE_GROUP",
      "priceGroup": {
        "groupId": "g_b289cc4fb8608d0a975df98d17df28d11ddd66727ff492846c5ac475ea8cc57c",
        "dataOrigin": "DEMO",
        "evidenceKind": "RECENT_SEARCH",
        "currency": "EUR",
        "scope": {
          "basis": "GROUP",
          "travellerCount": 1,
          "cabin": "ECONOMY",
          "coverage": "FULL_ITINERARY",
          "coveredSegmentIds": []
        },
        "taxes": "INCLUDED",
        "baggage": {
          "cabin": {
            "status": "UNKNOWN",
            "pieces": null,
            "weightKg": null
          },
          "checked": {
            "status": "UNKNOWN",
            "pieces": null,
            "weightKg": null
          }
        },
        "additionalCostKinds": [],
        "monetarilyComparable": true,
        "reasons": []
      }
    }
  ],
  "warnings": []
}
```

## Búsqueda con resultado parcial

La combinación de fechas 11–18 de febrero representa un escenario de fallo simulado por origen, reproducible con la misma versión.

```json
{
  "query": {
    "tripType": "ROUND_TRIP",
    "origins": [
      "OPO",
      "SCQ"
    ],
    "destination": "MAD",
    "outboundDate": "2027-02-11",
    "returnDate": "2027-02-18",
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1"
}
```

## Respuesta parcial

HTTP 200 PARTIAL. Se conserva la oferta OPO y la causa de SCQ. El proveedor demo simula la incidencia sin red.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000005",
  "context": {
    "datasetVersion": "demo-v1",
    "fingerprint": "q_2c5c7a282008ad8f7d4db93b4f152f144c4e8379be3fa850bc6fe7de2e582a82",
    "query": {
      "tripType": "ROUND_TRIP",
      "origins": [
        "OPO",
        "SCQ"
      ],
      "destination": "MAD",
      "outboundDate": "2027-02-11",
      "returnDate": "2027-02-18",
      "travellers": {
        "adults": 1
      },
      "cabin": "ECONOMY"
    },
    "dataOrigin": "DEMO",
    "evaluationInstant": "2027-01-01T00:00:00Z"
  },
  "status": "PARTIAL",
  "offers": [
    {
      "offerId": "of_17edbe8d9f093030dd2d81ddbf685fd55667fae9cd9844ce6579fde932c7ed60",
      "providerId": "DEMO",
      "dataOrigin": "DEMO",
      "itinerary": {
        "tripType": "ROUND_TRIP",
        "journeys": [
          {
            "direction": "OUTBOUND",
            "segments": [
              {
                "segmentId": "s-opo-outbound",
                "departureAirport": "OPO",
                "arrivalAirport": "MAD",
                "departureTime": {
                  "localDateTime": "2027-02-11T09:00:00",
                  "offset": "+00:00",
                  "zoneId": "Europe/Lisbon",
                  "instant": "2027-02-11T09:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-11T11:10:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-11T10:10:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D1",
                  "name": "Aerolínea Demo D1"
                },
                "operatingAirline": null,
                "flightNumber": "D1101",
                "technicalStops": 0,
                "durationSeconds": 4200
              }
            ],
            "connections": [],
            "durationSeconds": 4200
          },
          {
            "direction": "INBOUND",
            "segments": [
              {
                "segmentId": "s-opo-inbound",
                "departureAirport": "MAD",
                "arrivalAirport": "OPO",
                "departureTime": {
                  "localDateTime": "2027-02-18T18:00:00",
                  "offset": "+01:00",
                  "zoneId": "Europe/Madrid",
                  "instant": "2027-02-18T17:00:00Z",
                  "unknownReason": null
                },
                "arrivalTime": {
                  "localDateTime": "2027-02-18T18:10:00",
                  "offset": "+00:00",
                  "zoneId": "Europe/Lisbon",
                  "instant": "2027-02-18T18:10:00Z",
                  "unknownReason": null
                },
                "marketingAirline": {
                  "code": "D1",
                  "name": "Aerolínea Demo D1"
                },
                "operatingAirline": null,
                "flightNumber": "D1101",
                "technicalStops": 0,
                "durationSeconds": 4200
              }
            ],
            "connections": [],
            "durationSeconds": 4200
          }
        ],
        "durationSeconds": 8400,
        "connectionCount": 0
      },
      "price": {
        "money": {
          "amount": "100.00",
          "currency": "EUR"
        },
        "unknownReason": null,
        "evidenceKind": "RECENT_SEARCH",
        "scope": {
          "basis": "GROUP",
          "travellerCount": 1,
          "cabin": "ECONOMY",
          "coverage": "FULL_ITINERARY",
          "coveredSegmentIds": []
        },
        "taxes": "INCLUDED",
        "additionalCosts": [],
        "observedAt": "2026-12-31T23:55:00Z",
        "retrievedAt": "2027-01-01T00:00:00Z",
        "expiresAt": "2027-01-01T00:10:00Z",
        "freshness": "FRESH"
      },
      "seller": null,
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "comparisonGroupId": "g_4d078d4d699f0840818a2b776649c9fca6c12b9faa55173494482b3650f8cd44",
      "warnings": [
        {
          "code": "BAGGAGE_UNKNOWN",
          "path": "/baggage/checked",
          "message": "Equipaje facturado desconocido."
        }
      ]
    }
  ],
  "priceGroups": [
    {
      "groupId": "g_4d078d4d699f0840818a2b776649c9fca6c12b9faa55173494482b3650f8cd44",
      "dataOrigin": "DEMO",
      "evidenceKind": "RECENT_SEARCH",
      "currency": "EUR",
      "scope": {
        "basis": "GROUP",
        "travellerCount": 1,
        "cabin": "ECONOMY",
        "coverage": "FULL_ITINERARY",
        "coveredSegmentIds": []
      },
      "taxes": "INCLUDED",
      "baggage": {
        "cabin": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        },
        "checked": {
          "status": "UNKNOWN",
          "pieces": null,
          "weightKg": null
        }
      },
      "additionalCostKinds": [],
      "monetarilyComparable": true,
      "reasons": []
    }
  ],
  "observations": [],
  "observationCount": 0,
  "observationsTruncated": false,
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "baseCount": 1,
    "filteredCount": 1,
    "excludedUnknownCount": 0,
    "hasMore": false
  },
  "providerResults": [
    {
      "providerId": "DEMO",
      "origin": "OPO",
      "status": "COMPLETE",
      "requiredUnits": 1,
      "completedUnits": 1,
      "offerCount": 1,
      "observationCount": 0,
      "issues": []
    },
    {
      "providerId": "DEMO",
      "origin": "SCQ",
      "status": "FAILED",
      "requiredUnits": 1,
      "completedUnits": 0,
      "offerCount": 0,
      "observationCount": 0,
      "issues": [
        {
          "code": "PROVIDER_OFFLINE",
          "message": "Incidencia simulada de fuente desconectada."
        }
      ]
    }
  ],
  "warnings": [],
  "errors": [
    {
      "code": "PROVIDER_OFFLINE",
      "message": "Incidencia simulada de fuente desconectada."
    }
  ]
}
```

## Búsqueda vacía

La fecha 14 de febrero corresponde aquí a una consulta completada sin coincidencias.

```json
{
  "query": {
    "tripType": "ONE_WAY",
    "origins": [
      "OPO"
    ],
    "destination": "MAD",
    "outboundDate": "2027-02-14",
    "returnDate": null,
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1"
}
```

## Respuesta vacía

HTTP 200 COMPLETE con conteos cero y origen completado vacío.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000006",
  "context": {
    "datasetVersion": "demo-v1",
    "fingerprint": "q_7e64541efb32d042a2af002ced526493ed285a56917f966ddc0bed0e0a7dec78",
    "query": {
      "tripType": "ONE_WAY",
      "origins": [
        "OPO"
      ],
      "destination": "MAD",
      "outboundDate": "2027-02-14",
      "returnDate": null,
      "travellers": {
        "adults": 1
      },
      "cabin": "ECONOMY"
    },
    "dataOrigin": "DEMO",
    "evaluationInstant": "2027-01-01T00:00:00Z"
  },
  "status": "COMPLETE",
  "offers": [],
  "priceGroups": [],
  "observations": [],
  "observationCount": 0,
  "observationsTruncated": false,
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "baseCount": 0,
    "filteredCount": 0,
    "excludedUnknownCount": 0,
    "hasMore": false
  },
  "providerResults": [
    {
      "providerId": "DEMO",
      "origin": "OPO",
      "status": "EMPTY",
      "requiredUnits": 1,
      "completedUnits": 1,
      "offerCount": 0,
      "observationCount": 0,
      "issues": []
    }
  ],
  "warnings": [],
  "errors": []
}
```

## Solicitud inválida de ejemplo

El regreso obligatorio se omite semánticamente mediante null. Este bloque es un caso negativo deliberado.

```json
{
  "query": {
    "tripType": "ROUND_TRIP",
    "origins": [
      "SCQ",
      "OPO"
    ],
    "destination": "MAD",
    "outboundDate": "2027-02-10",
    "returnDate": null,
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1"
}
```

## Error de validación

HTTP 422, por ROUND_TRIP sin fecha de regreso. El cuerpo evita reflejar valores rechazados.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000007",
  "code": "VALIDATION_FAILED",
  "message": "Solicitud no válida.",
  "fieldErrors": [
    {
      "path": "/query/returnDate",
      "code": "REQUIRED"
    }
  ],
  "fieldErrorsTruncated": false,
  "providerResults": []
}
```

## Solicitud del escenario de fallo total

Esta consulta de referencia tiene una única unidad de origen cuyo timeout se simula en el corpus de contrato.

```json
{
  "query": {
    "tripType": "ONE_WAY",
    "origins": [
      "OPO"
    ],
    "destination": "MAD",
    "outboundDate": "2027-02-13",
    "returnDate": null,
    "travellers": {
      "adults": 1
    },
    "cabin": "ECONOMY"
  },
  "datasetVersion": "demo-v1"
}
```

## Error total

HTTP 503 para un escenario distinto con origen OPO y timeout simulado. Ninguna unidad completó. La consulta anterior tiene fechas diferentes de los casos completos, parciales y vacíos.

```json
{
  "contractVersion": "airwise-v1",
  "correlationId": "00000000-0000-4000-8000-000000000008",
  "code": "PROVIDERS_FAILED",
  "message": "La búsqueda no pudo completarse.",
  "fieldErrors": [],
  "fieldErrorsTruncated": false,
  "providerResults": [
    {
      "providerId": "DEMO",
      "origin": "OPO",
      "status": "FAILED",
      "requiredUnits": 1,
      "completedUnits": 0,
      "offerCount": 0,
      "observationCount": 0,
      "issues": [
        {
          "code": "PROVIDER_TIMEOUT",
          "message": "Tiempo de espera simulado agotado."
        }
      ]
    }
  ]
}
```

## Precio desconocido

Objeto PriceInformation válido. La evidencia RECENT_SEARCH se conserva aunque falte el importe.

```json
{
  "money": null,
  "unknownReason": "NOT_RETURNED",
  "evidenceKind": "RECENT_SEARCH",
  "scope": {
    "basis": "GROUP",
    "travellerCount": 1,
    "cabin": "ECONOMY",
    "coverage": "FULL_ITINERARY",
    "coveredSegmentIds": []
  },
  "taxes": "INCLUDED",
  "additionalCosts": [],
  "observedAt": "2026-12-31T23:55:00Z",
  "retrievedAt": "2027-01-01T00:00:00Z",
  "expiresAt": "2027-01-01T00:10:00Z",
  "freshness": "FRESH"
}
```

## Precio caducado

Objeto PriceInformation evaluado en el instante del escenario. La recuperación posterior a expiresAt conserva EXPIRED.

```json
{
  "money": {
    "amount": "120.00",
    "currency": "EUR"
  },
  "unknownReason": null,
  "evidenceKind": "OBSERVED_CACHE",
  "scope": {
    "basis": "GROUP",
    "travellerCount": 1,
    "cabin": "ECONOMY",
    "coverage": "FULL_ITINERARY",
    "coveredSegmentIds": []
  },
  "taxes": "INCLUDED",
  "additionalCosts": [],
  "observedAt": "2026-12-30T12:00:00Z",
  "retrievedAt": "2027-01-01T00:00:00Z",
  "expiresAt": "2026-12-31T23:00:00Z",
  "freshness": "EXPIRED"
}
```

## Vectores de identidad

El primer caso utiliza recordKey `opo-mad-oneway`. El payload canónico y sus identificadores esperados permiten contrastar implementaciones Java sin depender del orden de filtros o páginas.

```json
{
  "canonicalPayload": "{\"contractVersion\":\"airwise-v1\",\"datasetVersion\":\"demo-v1\",\"query\":{\"tripType\":\"ONE_WAY\",\"origins\":[\"OPO\"],\"destination\":\"MAD\",\"outboundDate\":\"2027-02-10\",\"returnDate\":null,\"travellers\":{\"adults\":1},\"cabin\":\"ECONOMY\"}}",
  "fingerprint": "q_f4cb1de712e0a33f1260c695fb0ccdf53d6f4da2ec05fa534625969aac8dd930",
  "offerId": "of_f04264b1cb41c28788bb5f40d3c18f96f88966050d68ffc6f3f3cc286afc3c83",
  "recordKey": "opo-mad-oneway",
  "groupKey": "{\"fingerprint\":\"q_f4cb1de712e0a33f1260c695fb0ccdf53d6f4da2ec05fa534625969aac8dd930\",\"dataOrigin\":\"DEMO\",\"evidenceKind\":\"RECENT_SEARCH\",\"currency\":\"EUR\",\"scope\":{\"basis\":\"GROUP\",\"travellerCount\":1,\"cabin\":\"ECONOMY\",\"coverage\":\"FULL_ITINERARY\",\"coveredSegmentIds\":[]},\"taxes\":\"INCLUDED\",\"baggage\":{\"cabin\":{\"status\":\"UNKNOWN\",\"pieces\":null,\"weightKg\":null},\"checked\":{\"status\":\"UNKNOWN\",\"pieces\":null,\"weightKg\":null}},\"additionalCostKinds\":[]}",
  "groupId": "g_35e6380641ec2860a4ddc4ff437324314a8d55316744695a07c26210f66ad190"
}
```
