
# Production Shared Schema Baseline
Fecha de inspección inicial: 2026-09-02
Fecha de consolidación del documento: 2026-09-03
Ambiente: Producción Render
Base de datos: esquema compartido por:
- Módulo cascada de viajes
- Módulo monitoreo de pasajeros

## Objetivo
Documentar el estado actual real del esquema compartido antes de integrar Flyway y definir el baseline inicial.

## Alcance
Esta base de datos no pertenece a un solo módulo. El esquema actual ya estaba en uso antes del módulo de pasajeros y
hoy es compartido por:
- cascada de viajes
- monitoreo de pasajeros

## Tablas existentes
| table_name                         |
| ---------------------------------- |
| boarding_events                    |
| cascada_recipients                 |
| cascada_standard_cell              |
| cascada_standard_manual_row        |
| cascada_standard_week              |
| companies                          |
| driver_plant_assignments           |
| driver_shifts                      |
| drivers                            |
| drivers_routes                     |
| flexsur_detail                     |
| flexsur_driver_assignment          |
| flexsur_driver_assignment_days     |
| flexsur_manual_row                 |
| flexsur_service                    |
| flexsur_service_driver_assignment  |
| flexsur_week                       |
| flexsur_week_totals                |
| flexsur_week_totals_by_day         |
| format_catalog                     |
| format_turn_config                 |
| format_type                        |
| format_type_te_rule                |
| format_week                        |
| format_week_cell                   |
| format_week_manual_row             |
| format_week_totals                 |
| format_week_totals_unit_type       |
| inbox_message_user_state           |
| login_request                      |
| passenger_boarding_event_locations |
| passenger_groups                   |
| passengers                         |
| permissions                        |
| plants                             |
| regal_detail                       |
| regal_manual_row                   |
| regal_trip_type                    |
| regal_trip_type_days               |
| regal_week                         |
| regal_week_summary                 |
| report_executions                  |
| role_companies                     |
| role_permissions                   |
| role_plants                        |
| roles                              |
| routes                             |
| shift_days                         |
| shift_format_turn_map              |
| shift_long_week_days               |
| shift_short_week_days              |
| shift_wialon_aliases               |
| shifts                             |
| units                              |
| users                              |
| users_companies                    |
| users_plants                       |
| wialon_sessions                    |

## Columnas de todas las tablas
| table_name | column_name | data_type | is_nullable | column_default |
|---|---|---|---|---|
| boarding_events | boarding_event_id | bigint | NO |  |
| boarding_events | alighting_time | timestamp without time zone | YES |  |
| boarding_events | boarding_time | timestamp without time zone | NO |  |
| boarding_events | created_at | timestamp without time zone | NO |  |
| boarding_events | duration | character varying | YES |  |
| boarding_events | end_latitude | double precision | YES |  |
| boarding_events | end_location_text | character varying | YES |  |
| boarding_events | end_longitude | double precision | YES |  |
| boarding_events | final_time | timestamp without time zone | YES |  |
| boarding_events | raw_row_json | text | YES |  |
| boarding_events | row_number | character varying | YES |  |
| boarding_events | shift | character varying | YES |  |
| boarding_events | start_latitude | double precision | YES |  |
| boarding_events | start_location_text | character varying | YES |  |
| boarding_events | start_longitude | double precision | YES |  |
| boarding_events | updated_at | timestamp without time zone | NO |  |
| boarding_events | wialon_row_key | character varying | NO |  |
| boarding_events | wialon_tag_id | character varying | YES |  |
| boarding_events | passenger_id | bigint | NO |  |
| boarding_events | passenger_group_id | bigint | NO |  |
| boarding_events | plant_id | bigint | NO |  |
| boarding_events | report_execution_id | bigint | NO |  |
| boarding_events | unit_id | bigint | NO |  |
| cascada_recipients | cascada_recipient_id | bigint | NO |  |
| cascada_recipients | cascada_type | character varying | NO |  |
| cascada_recipients | day_key | character varying | YES |  |
| cascada_recipients | recipient_user_id | bigint | NO |  |
| cascada_recipients | sent_at | timestamp without time zone | YES |  |
| cascada_recipients | sent_by_user_id | bigint | YES |  |
| cascada_recipients | shift_id | character varying | NO |  |
| cascada_recipients | week_date | date | NO |  |
| cascada_recipients | plant_id | bigint | NO |  |
| cascada_standard_cell | cascada_standard_cell_id | bigint | NO |  |
| cascada_standard_cell | day_key | character varying | NO |  |
| cascada_standard_cell | driver_name_override | character varying | YES |  |
| cascada_standard_cell | val_e | character varying | YES |  |
| cascada_standard_cell | val_ete | character varying | YES |  |
| cascada_standard_cell | route_id | bigint | YES |  |
| cascada_standard_cell | val_s | character varying | YES |  |
| cascada_standard_cell | val_ste | character varying | YES |  |
| cascada_standard_cell | driver_id | bigint | YES |  |
| cascada_standard_cell | manual_standard_row_id | bigint | YES |  |
| cascada_standard_cell | cascada_standard_week_id | bigint | NO |  |
| cascada_standard_manual_row | manual_standard_row_id | bigint | NO |  |
| cascada_standard_manual_row | driver_name_override | character varying | YES |  |
| cascada_standard_manual_row | route_id | bigint | YES |  |
| cascada_standard_manual_row | route_name | character varying | YES |  |
| cascada_standard_manual_row | sort_order | integer | YES |  |
| cascada_standard_manual_row | updated_at | timestamp without time zone | YES |  |
| cascada_standard_manual_row | updated_by_user_id | bigint | YES |  |
| cascada_standard_manual_row | week_date | date | NO |  |
| cascada_standard_manual_row | plant_id | bigint | NO |  |
| cascada_standard_week | cascada_standard_week_id | bigint | NO |  |
| cascada_standard_week | sent_at | timestamp without time zone | YES |  |
| cascada_standard_week | sent_by_user_id | bigint | YES |  |
| cascada_standard_week | shift_id | character varying | NO |  |
| cascada_standard_week | status | character varying | YES |  |
| cascada_standard_week | updated_at | timestamp without time zone | YES |  |
| cascada_standard_week | updated_by_user_id | bigint | YES |  |
| cascada_standard_week | week_end_date | date | YES |  |
| cascada_standard_week | week_number | integer | YES |  |
| cascada_standard_week | week_start_date | date | NO |  |
| cascada_standard_week | plant_id | bigint | NO |  |
| companies | company_id | bigint | NO |  |
| companies | company_name | character varying | YES |  |
| driver_plant_assignments | driver_plant_assignment_id | bigint | NO |  |
| driver_plant_assignments | driver_type | character varying | YES |  |
| driver_plant_assignments | driver_id | bigint | NO |  |
| driver_plant_assignments | plant_id | bigint | NO |  |
| driver_plant_assignments | route_id | bigint | YES |  |
| driver_shifts | driver_id | bigint | NO |  |
| driver_shifts | shift_id | bigint | NO |  |
| drivers | driver_id | bigint | NO |  |
| drivers | active | boolean | YES |  |
| drivers | driver_name | character varying | YES |  |
| drivers | email | character varying | YES |  |
| drivers | last_name | character varying | YES |  |
| drivers | plant_id | bigint | YES |  |
| drivers_routes | driver_route_id | bigint | NO |  |
| drivers_routes | assigment_date | timestamp without time zone | YES |  |
| drivers_routes | driver_type | character varying | YES |  |
| drivers_routes | notes | character varying | YES |  |
| drivers_routes | shift | character varying | YES |  |
| drivers_routes | driver_id | bigint | YES |  |
| drivers_routes | route_id | bigint | YES |  |
| flexsur_detail | detail_id | bigint | NO |  |
| flexsur_detail | extra_column | integer | NO |  |
| flexsur_detail | service_date | date | NO |  |
| flexsur_detail | total | integer | NO |  |
| flexsur_detail | trips | integer | NO |  |
| flexsur_detail | flexsur_week_id | bigint | NO |  |
| flexsur_driver_assignment | flexsur_driver_assignment_id | bigint | NO |  |
| flexsur_driver_assignment | active | boolean | NO |  |
| flexsur_driver_assignment | driver_type | character varying | NO |  |
| flexsur_driver_assignment | route_location | character varying | YES |  |
| flexsur_driver_assignment | updated_at | timestamp without time zone | YES |  |
| flexsur_driver_assignment | updated_by_user_id | bigint | YES |  |
| flexsur_driver_assignment | driver_id | bigint | NO |  |
| flexsur_driver_assignment | plant_id | bigint | NO |  |
| flexsur_driver_assignment | route_id | bigint | YES |  |
| flexsur_driver_assignment | service_id | bigint | NO |  |
| flexsur_driver_assignment | shift_id | bigint | NO |  |
| flexsur_driver_assignment_days | flexsur_driver_assignment_id | bigint | NO |  |
| flexsur_driver_assignment_days | day_key | character varying | NO |  |
| flexsur_manual_row | manual_flexsur_row_id | bigint | NO |  |
| flexsur_manual_row | service_name | character varying | NO |  |
| flexsur_manual_row | sort_order | integer | YES |  |
| flexsur_manual_row | updated_at | timestamp without time zone | YES |  |
| flexsur_manual_row | updated_by_user_id | bigint | YES |  |
| flexsur_manual_row | week_date | date | NO |  |
| flexsur_manual_row | plant_id | bigint | NO |  |
| flexsur_manual_row | shift_id | bigint | YES |  |
| flexsur_service | service_id | bigint | NO |  |
| flexsur_service | active | boolean | NO |  |
| flexsur_service | service_name | character varying | NO |  |
| flexsur_service | sort_order | integer | YES |  |
| flexsur_service | plant_id | bigint | NO |  |
| flexsur_service | service_time | time without time zone | YES |  |
| flexsur_service | service_type | character varying | YES |  |
| flexsur_service | special_week_type | character varying | YES |  |
| flexsur_service | shift_id | bigint | YES |  |
| flexsur_service_driver_assignment | flexsur_service_driver_assignment_id | bigint | NO |  |
| flexsur_service_driver_assignment | active | boolean | NO |  |
| flexsur_service_driver_assignment | updated_at | timestamp without time zone | YES |  |
| flexsur_service_driver_assignment | updated_by_user_id | bigint | YES |  |
| flexsur_service_driver_assignment | driver_id | bigint | NO |  |
| flexsur_service_driver_assignment | plant_id | bigint | NO |  |
| flexsur_service_driver_assignment | service_id | bigint | NO |  |
| flexsur_week | flexsur_week_id | bigint | NO |  |
| flexsur_week | sent_at | timestamp without time zone | YES |  |
| flexsur_week | sent_by_user_id | bigint | YES |  |
| flexsur_week | service_name | character varying | NO |  |
| flexsur_week | status | character varying | NO |  |
| flexsur_week | updated_at | timestamp without time zone | YES |  |
| flexsur_week | updated_by_user_id | bigint | YES |  |
| flexsur_week | week_date | date | NO |  |
| flexsur_week | manual_flexsur_row_id | bigint | YES |  |
| flexsur_week | plant_id | bigint | NO |  |
| flexsur_week | shift_id | bigint | YES |  |
| flexsur_week_totals | flexsur_week_totals_id | bigint | NO |  |
| flexsur_week_totals | updated_at | timestamp without time zone | YES |  |
| flexsur_week_totals | updated_by_user_id | bigint | YES |  |
| flexsur_week_totals | week_date | date | NO |  |
| flexsur_week_totals | week_total | integer | NO |  |
| flexsur_week_totals | plant_id | bigint | NO |  |
| flexsur_week_totals | shift_id | bigint | YES |  |
| flexsur_week_totals_by_day | flexsur_week_totals_id | bigint | NO |  |
| flexsur_week_totals_by_day | day_total | integer | NO |  |
| flexsur_week_totals_by_day | service_date | date | NO |  |
| format_catalog | format_catalog_id | bigint | NO |  |
| format_catalog | active | boolean | NO |  |
| format_catalog | format_category | character varying | NO |  |
| format_catalog | format_code | character varying | NO |  |
| format_catalog | name | character varying | NO |  |
| format_turn_config | turn_config_id | bigint | NO |  |
| format_turn_config | day_of_week | character varying | NO |  |
| format_turn_config | sort_order | integer | NO |  |
| format_turn_config | turn_name | character varying | NO |  |
| format_turn_config | format_type_id | bigint | NO |  |
| format_type | format_type_id | bigint | NO |  |
| format_type | format_catalog_id | bigint | YES |  |
| format_type | includes_unit_type | boolean | NO |  |
| format_type | name | character varying | NO |  |
| format_type | secondary_column | character varying | YES |  |
| format_type_te_rule | format_type_te_rule_id | bigint | NO |  |
| format_type_te_rule | active | boolean | NO |  |
| format_type_te_rule | day_of_week | character varying | NO |  |
| format_type_te_rule | te_count | integer | NO |  |
| format_type_te_rule | format_type_id | bigint | NO |  |
| format_week | format_week_id | bigint | NO |  |
| format_week | driver_last_name_override | character varying | YES |  |
| format_week | driver_name_override | character varying | YES |  |
| format_week | route_name_override | character varying | YES |  |
| format_week | secondary_value | character varying | YES |  |
| format_week | sent_at | timestamp without time zone | YES |  |
| format_week | sent_by_user_id | bigint | YES |  |
| format_week | status | character varying | NO |  |
| format_week | unit_type | character varying | YES |  |
| format_week | updated_at | timestamp without time zone | YES |  |
| format_week | updated_by_user_id | bigint | YES |  |
| format_week | week_date | date | NO |  |
| format_week | week_end_date | date | YES |  |
| format_week | week_number | integer | YES |  |
| format_week | week_start_date | date | YES |  |
| format_week | driver_id | bigint | YES |  |
| format_week | format_type_id | bigint | NO |  |
| format_week | manual_row_id | bigint | YES |  |
| format_week | plant_id | bigint | NO |  |
| format_week | route_id | bigint | YES |  |
| format_week | shift_id | bigint | NO |  |
| format_week_cell | cell_id | bigint | NO |  |
| format_week_cell | day_of_week | character varying | NO |  |
| format_week_cell | trip_count | integer | NO |  |
| format_week_cell | format_week_id | bigint | NO |  |
| format_week_cell | turn_config_id | bigint | NO |  |
| format_week_manual_row | manual_row_id | bigint | NO |  |
| format_week_manual_row | driver_last_name | character varying | YES |  |
| format_week_manual_row | driver_name | character varying | YES |  |
| format_week_manual_row | extra_row | boolean | NO |  |
| format_week_manual_row | route_name | character varying | YES |  |
| format_week_manual_row | secondary_value | character varying | YES |  |
| format_week_manual_row | sort_order | integer | YES |  |
| format_week_manual_row | unit_type | character varying | YES |  |
| format_week_manual_row | updated_at | timestamp without time zone | YES |  |
| format_week_manual_row | updated_by_user_id | bigint | YES |  |
| format_week_manual_row | week_date | date | NO |  |
| format_week_manual_row | format_type_id | bigint | NO |  |
| format_week_manual_row | plant_id | bigint | NO |  |
| format_week_totals | format_week_totals_id | bigint | NO |  |
| format_week_totals | updated_at | timestamp without time zone | YES |  |
| format_week_totals | updated_by_user_id | bigint | YES |  |
| format_week_totals | week_date | date | NO |  |
| format_week_totals | format_type_id | bigint | NO |  |
| format_week_totals | plant_id | bigint | NO |  |
| format_week_totals | shift_id | bigint | YES |  |
| format_week_totals_unit_type | format_week_totals_id | bigint | NO |  |
| format_week_totals_unit_type | total | integer | NO |  |
| format_week_totals_unit_type | unit_type | character varying | NO |  |
| inbox_message_user_state | id | bigint | NO |  |
| inbox_message_user_state | created_at | timestamp without time zone | NO |  |
| inbox_message_user_state | first_seen_version | timestamp without time zone | YES |  |
| inbox_message_user_state | last_seen_version | timestamp without time zone | YES |  |
| inbox_message_user_state | message_id | character varying | NO |  |
| inbox_message_user_state | opened_version | timestamp without time zone | YES |  |
| inbox_message_user_state | updated_at | timestamp without time zone | NO |  |
| inbox_message_user_state | user_id | bigint | NO |  |
| login_request | id | bigint | NO |  |
| login_request | password | character varying | YES |  |
| login_request | user_name | character varying | YES |  |
| passenger_boarding_event_locations | location_id | bigint | NO |  |
| passenger_boarding_event_locations | address | character varying | NO |  |
| passenger_boarding_event_locations | geofence_name | character varying | YES |  |
| passenger_boarding_event_locations | latitude | double precision | NO |  |
| passenger_boarding_event_locations | longitude | double precision | NO |  |
| passenger_groups | passenger_group_id | bigint | NO |  |
| passenger_groups | is_active | boolean | NO |  |
| passenger_groups | is_default | boolean | NO |  |
| passenger_groups | last_synced_at | timestamp without time zone | YES |  |
| passenger_groups | name | character varying | NO |  |
| passenger_groups | wialon_id | bigint | NO |  |
| passenger_groups | plant_id | bigint | NO |  |
| passengers | passenger_id | bigint | NO |  |
| passengers | card_number | character varying | YES |  |
| passengers | created_at | timestamp without time zone | NO |  |
| passengers | name | character varying | YES |  |
| passengers | updated_at | timestamp without time zone | NO |  |
| passengers | wialon_passenger_id | character varying | NO |  |
| permissions | permission_id | bigint | NO |  |
| permissions | code | character varying | NO |  |
| permissions | description | character varying | YES |  |
| plants | plant_id | bigint | NO |  |
| plants | format_catalog_id | bigint | NO |  |
| plants | format_type_id | bigint | YES |  |
| plants | location | character varying | YES |  |
| plants | plant_name | character varying | YES |  |
| plants | company_id | bigint | NO |  |
| plants | last_synced_at | timestamp without time zone | YES |  |
| plants | template_id | bigint | YES |  |
| plants | wialon_id | bigint | YES |  |
| plants | wialon_units_group_id | bigint | YES |  |
| plants | active | boolean | NO | true |
| regal_detail | detail_id | bigint | NO |  |
| regal_detail | day_of_week | character varying | NO |  |
| regal_detail | trip_count | integer | NO |  |
| regal_detail | regal_week_id | bigint | NO |  |
| regal_detail | trip_type_id | bigint | NO |  |
| regal_manual_row | manual_regal_row_id | bigint | NO |  |
| regal_manual_row | driver_name_override | character varying | NO |  |
| regal_manual_row | sort_order | integer | YES |  |
| regal_manual_row | updated_at | timestamp without time zone | YES |  |
| regal_manual_row | updated_by_user_id | bigint | YES |  |
| regal_manual_row | week_date | date | NO |  |
| regal_manual_row | plant_id | bigint | NO |  |
| regal_manual_row | route_location | character varying | YES |  |
| regal_manual_row | route_name | character varying | YES |  |
| regal_trip_type | trip_type_id | bigint | NO |  |
| regal_trip_type | active | boolean | NO |  |
| regal_trip_type | code | character varying | NO |  |
| regal_trip_type | label | character varying | NO |  |
| regal_trip_type | sort_order | integer | NO |  |
| regal_trip_type | plant_id | bigint | NO |  |
| regal_trip_type_days | trip_type_id | bigint | NO |  |
| regal_trip_type_days | day_key | character varying | NO |  |
| regal_week | regal_week_id | bigint | NO |  |
| regal_week | sent_at | timestamp without time zone | YES |  |
| regal_week | sent_by_user_id | bigint | YES |  |
| regal_week | status | character varying | NO |  |
| regal_week | updated_at | timestamp without time zone | YES |  |
| regal_week | updated_by_user_id | bigint | YES |  |
| regal_week | week_date | date | NO |  |
| regal_week | driver_id | bigint | YES |  |
| regal_week | manual_regal_row_id | bigint | YES |  |
| regal_week | plant_id | bigint | NO |  |
| regal_week | shift_id | bigint | YES |  |
| regal_week_summary | regal_week_summary_id | bigint | NO |  |
| regal_week_summary | extra_long | integer | NO |  |
| regal_week_summary | extra_short | integer | NO |  |
| regal_week_summary | normal_long | integer | NO |  |
| regal_week_summary | normal_short | integer | NO |  |
| regal_week_summary | updated_at | timestamp without time zone | YES |  |
| regal_week_summary | updated_by_user_id | bigint | YES |  |
| regal_week_summary | week_date | date | NO |  |
| regal_week_summary | plant_id | bigint | NO |  |
| report_executions | report_execution_id | bigint | NO |  |
| report_executions | duration_ms | integer | NO |  |
| report_executions | error_message | character varying | YES |  |
| report_executions | executed_at | timestamp without time zone | NO |  |
| report_executions | finished_at | timestamp without time zone | YES |  |
| report_executions | interval_from | timestamp without time zone | NO |  |
| report_executions | interval_to | timestamp without time zone | NO |  |
| report_executions | parser_version | character varying | YES |  |
| report_executions | report_object_id | bigint | NO |  |
| report_executions | report_object_sec_id | bigint | NO |  |
| report_executions | report_resource_id | bigint | NO |  |
| report_executions | report_template_id | bigint | NO |  |
| report_executions | request_key | character varying | YES |  |
| report_executions | row_count | integer | NO |  |
| report_executions | sid_used | character varying | YES |  |
| report_executions | status | character varying | NO |  |
| report_executions | total_rows | integer | NO |  |
| role_companies | role_company_id | bigint | NO |  |
| role_companies | created_at | timestamp without time zone | YES |  |
| role_companies | company_id | bigint | NO |  |
| role_companies | role_id | bigint | NO |  |
| role_permissions | role_permission_id | bigint | NO |  |
| role_permissions | created_at | timestamp without time zone | YES |  |
| role_permissions | permission_id | bigint | NO |  |
| role_permissions | role_id | bigint | NO |  |
| role_plants | role_plant_id | bigint | NO |  |
| role_plants | created_at | timestamp without time zone | YES |  |
| role_plants | plant_id | bigint | NO |  |
| role_plants | role_id | bigint | NO |  |
| roles | role_id | bigint | NO |  |
| roles | description | character varying | YES |  |
| roles | role_name | character varying | YES |  |
| roles | role_key | character varying | YES |  |
| routes | route_id | bigint | NO |  |
| routes | location | character varying | YES |  |
| routes | route_name | character varying | YES |  |
| routes | unit_type | character varying | YES |  |
| routes | plant_id | bigint | NO |  |
| shift_days | shift_id | bigint | NO |  |
| shift_days | day_key | character varying | NO |  |
| shift_format_turn_map | map_id | bigint | NO |  |
| shift_format_turn_map | day_of_week | character varying | NO |  |
| shift_format_turn_map | turn_name | character varying | NO |  |
| shift_format_turn_map | format_type_id | bigint | NO |  |
| shift_format_turn_map | plant_id | bigint | NO |  |
| shift_format_turn_map | shift_id | bigint | NO |  |
| shift_long_week_days | shift_id | bigint | NO |  |
| shift_long_week_days | day_key | character varying | NO |  |
| shift_short_week_days | shift_id | bigint | NO |  |
| shift_short_week_days | day_key | character varying | NO |  |
| shift_wialon_aliases | shift_wialon_alias_id | bigint | NO |  |
| shift_wialon_aliases | alias_name | character varying | NO |  |
| shift_wialon_aliases | normalized_alias_name | character varying | NO |  |
| shift_wialon_aliases | shift_id | bigint | NO |  |
| shifts | shift_id | bigint | NO |  |
| shifts | end_time | time without time zone | NO |  |
| shifts | shift_name | character varying | NO |  |
| shifts | start_time | time without time zone | NO |  |
| shifts | plant_id | bigint | NO |  |
| shifts | shift_type | character varying | YES |  |
| shifts | active | boolean | NO | true |
| units | unit_id | bigint | NO |  |
| units | internal_id | character varying | YES |  |
| units | is_active | boolean | NO |  |
| units | last_synced_at | timestamp without time zone | YES |  |
| units | name_raw | character varying | NO |  |
| units | route_code | character varying | YES |  |
| units | route_name | character varying | YES |  |
| units | wialon_id | bigint | NO |  |
| units | plant_id | bigint | NO |  |
| users | user_id | bigint | NO |  |
| users | user_active | boolean | YES |  |
| users | create_at | timestamp without time zone | YES |  |
| users | email | character varying | YES |  |
| users | last_name | character varying | YES |  |
| users | full_name | character varying | YES |  |
| users | user_password | character varying | YES |  |
| users | phone | character varying | YES |  |
| users | user_name | character varying | YES |  |
| users | role_id | bigint | NO |  |
| users_companies | id_users_companies | bigint | NO |  |
| users_companies | fecha_asignacion | timestamp without time zone | YES |  |
| users_companies | company_id | bigint | NO |  |
| users_companies | user_id | bigint | NO |  |
| users_plants | user_plant_id | bigint | NO |  |
| users_plants | assignment_date | timestamp without time zone | YES |  |
| users_plants | plant_id | bigint | NO |  |
| users_plants | user_id | bigint | NO |  |
| wialon_sessions | wialon_id | bigint | NO |  |
| wialon_sessions | created_at | timestamp without time zone | NO |  |
| wialon_sessions | expires_at | timestamp without time zone | NO |  |
| wialon_sessions | is_active | boolean | NO |  |
| wialon_sessions | last_used_at | timestamp without time zone | NO |  |
| wialon_sessions | sid | character varying | NO |  |
| wialon_sessions | token | character varying | NO |  |

## Primary keys
| table_name | constraint_name | column_name |
|---|---|---|
| boarding_events | boarding_events_pkey | boarding_event_id |
| cascada_recipients | cascada_recipients_pkey | cascada_recipient_id |
| cascada_standard_cell | cascada_standard_cell_pkey | cascada_standard_cell_id |
| cascada_standard_manual_row | cascada_standard_manual_row_pkey | manual_standard_row_id |
| cascada_standard_week | cascada_standard_week_pkey | cascada_standard_week_id |
| companies | companies_pkey | company_id |
| driver_plant_assignments | driver_plant_assignments_pkey | driver_plant_assignment_id |
| driver_shifts | driver_shifts_pkey | driver_id |
| driver_shifts | driver_shifts_pkey | shift_id |
| drivers | drivers_pkey | driver_id |
| drivers_routes | drivers_routes_pkey | driver_route_id |
| flexsur_detail | flexsur_detail_pkey | detail_id |
| flexsur_driver_assignment | flexsur_driver_assignment_pkey | flexsur_driver_assignment_id |
| flexsur_driver_assignment_days | flexsur_driver_assignment_days_pkey | flexsur_driver_assignment_id |
| flexsur_driver_assignment_days | flexsur_driver_assignment_days_pkey | day_key |
| flexsur_manual_row | flexsur_manual_row_pkey | manual_flexsur_row_id |
| flexsur_service | flexsur_service_pkey | service_id |
| flexsur_service_driver_assignment | flexsur_service_driver_assignment_pkey | flexsur_service_driver_assignment_id |
| flexsur_week | flexsur_week_pkey | flexsur_week_id |
| flexsur_week_totals | flexsur_week_totals_pkey | flexsur_week_totals_id |
| flexsur_week_totals_by_day | flexsur_week_totals_by_day_pkey | flexsur_week_totals_id |
| flexsur_week_totals_by_day | flexsur_week_totals_by_day_pkey | service_date |
| format_catalog | format_catalog_pkey | format_catalog_id |
| format_turn_config | format_turn_config_pkey | turn_config_id |
| format_type | format_type_pkey | format_type_id |
| format_type_te_rule | format_type_te_rule_pkey | format_type_te_rule_id |
| format_week | format_week_pkey | format_week_id |
| format_week_cell | format_week_cell_pkey | cell_id |
| format_week_manual_row | format_week_manual_row_pkey | manual_row_id |
| format_week_totals | format_week_totals_pkey | format_week_totals_id |
| format_week_totals_unit_type | format_week_totals_unit_type_pkey | format_week_totals_id |
| format_week_totals_unit_type | format_week_totals_unit_type_pkey | unit_type |
| inbox_message_user_state | inbox_message_user_state_pkey | id |
| login_request | login_request_pkey | id |
| passenger_boarding_event_locations | passenger_boarding_event_locations_pkey | location_id |
| passenger_groups | passenger_groups_pkey | passenger_group_id |
| passengers | passengers_pkey | passenger_id |
| permissions | permissions_pkey | permission_id |
| plants | plants_pkey | plant_id |
| regal_detail | regal_detail_pkey | detail_id |
| regal_manual_row | regal_manual_row_pkey | manual_regal_row_id |
| regal_trip_type | regal_trip_type_pkey | trip_type_id |
| regal_trip_type_days | regal_trip_type_days_pkey | trip_type_id |
| regal_trip_type_days | regal_trip_type_days_pkey | day_key |
| regal_week | regal_week_pkey | regal_week_id |
| regal_week_summary | regal_week_summary_pkey | regal_week_summary_id |
| report_executions | report_executions_pkey | report_execution_id |
| role_companies | role_companies_pkey | role_company_id |
| role_permissions | role_permissions_pkey | role_permission_id |
| role_plants | role_plants_pkey | role_plant_id |
| roles | roles_pkey | role_id |
| routes | routes_pkey | route_id |
| shift_days | shift_days_pkey | shift_id |
| shift_days | shift_days_pkey | day_key |
| shift_format_turn_map | shift_format_turn_map_pkey | map_id |
| shift_long_week_days | shift_long_week_days_pkey | shift_id |
| shift_long_week_days | shift_long_week_days_pkey | day_key |
| shift_short_week_days | shift_short_week_days_pkey | shift_id |
| shift_short_week_days | shift_short_week_days_pkey | day_key |
| shift_wialon_aliases | shift_wialon_aliases_pkey | shift_wialon_alias_id |
| shifts | shifts_pkey | shift_id |
| units | units_pkey | unit_id |
| users | users_pkey | user_id |
| users_companies | users_companies_pkey | id_users_companies |
| users_plants | users_plants_pkey | user_plant_id |
| wialon_sessions | wialon_sessions_pkey | wialon_id |

## Foregein Keys
| table_name | constraint_name | column_name | foreign_table_name | foreign_column_name |
|---|---|---|---|---|
| boarding_events | fkejy4vg9s2lbtbsfl1pdqxbvx3 | passenger_id | passengers | passenger_id |
| boarding_events | fkhew9von0l6wb5yfwnexo8ex7w | passenger_group_id | passenger_groups | passenger_group_id |
| boarding_events | fkkiex7ilg6x9vg0eck1p3eg233 | report_execution_id | report_executions | report_execution_id |
| boarding_events | fknpjsghd1uhp6hmirc5xmxe39n | unit_id | units | unit_id |
| boarding_events | fkrcrdat6qadn8c8vmrw7nuad4g | plant_id | plants | plant_id |
| cascada_recipients | fks1na6sq7dwb058o8s01lo42tm | plant_id | plants | plant_id |
| cascada_standard_cell | fk14rup5r0f5hex9pnhytdixpd2 | driver_id | drivers | driver_id |
| cascada_standard_cell | fkco16atfr33mssl0pp3fp71ase | cascada_standard_week_id | cascada_standard_week | cascada_standard_week_id |
| cascada_standard_cell | fkkogigd2xrl1qlemjkt9bitj7d | manual_standard_row_id | cascada_standard_manual_row | manual_standard_row_id |
| cascada_standard_manual_row | fka0g4f2y4dhap1gqdtabykpcj2 | plant_id | plants | plant_id |
| cascada_standard_week | fk6sa7d4jt26jpkyk1ghdoi8gdw | plant_id | plants | plant_id |
| driver_plant_assignments | fk96tlhwj40ghgja17yd6ucjhbx | plant_id | plants | plant_id |
| driver_plant_assignments | fki92n06iy8lpy8rdt4jnkrtvt | route_id | routes | route_id |
| driver_plant_assignments | fknrik2mdm5y613b08ule5g4xjb | driver_id | drivers | driver_id |
| driver_shifts | fk3wg4dy5prbnm8wgks02syt2nh | driver_id | drivers | driver_id |
| driver_shifts | fkjpenr1v05f40j3n0vcjdsuju8 | shift_id | shifts | shift_id |
| drivers | fkr393c6lft794txqccr5chph3s | plant_id | plants | plant_id |
| drivers_routes | fka4xemhl5gshwwyn8ls5dujba7 | driver_id | drivers | driver_id |
| drivers_routes | fkr5c4invag7rt8o19uv1w7c2mj | route_id | routes | route_id |
| flexsur_detail | fkmy4pcmqjjncyyq6ytnwnglbtw | flexsur_week_id | flexsur_week | flexsur_week_id |
| flexsur_driver_assignment | fk1fygvpn4ukisqi0n3s6vwpjh4 | shift_id | shifts | shift_id |
| flexsur_driver_assignment | fk9tgk6ey5rwuhf1y9f7xb18u9u | route_id | routes | route_id |
| flexsur_driver_assignment | fkdblvu06lcccth2riukqoagicb | plant_id | plants | plant_id |
| flexsur_driver_assignment | fkhp1f79tpn73g8bj1vg4j15g7i | driver_id | drivers | driver_id |
| flexsur_driver_assignment | fkloie2g0ibhgy64e42w2iq7bxt | service_id | flexsur_service | service_id |
| flexsur_driver_assignment_days | fkfkohm7xdca9de0yy8gq8wylus | flexsur_driver_assignment_id | flexsur_driver_assignment | flexsur_driver_assignment_id |
| flexsur_manual_row | fkerw5xrqc4ydl7pcw0igh4il1t | plant_id | plants | plant_id |
| flexsur_manual_row | fkr81xsbggd6f06c2s5mxnx1t8t | shift_id | shifts | shift_id |
| flexsur_service | fkdcwlavd6j64bps5y1w0ib4vr6 | shift_id | shifts | shift_id |
| flexsur_service | fkmgxiqi31f4od224e52biy05kl | plant_id | plants | plant_id |
| flexsur_service_driver_assignment | fk3hgft5ipd2q1wbdfdlcv5u02v | plant_id | plants | plant_id |
| flexsur_service_driver_assignment | fkghlo3hf5f5lgtu6d9e7nu6b1w | service_id | flexsur_service | service_id |
| flexsur_service_driver_assignment | fkijtm8qxywckpnq28elh5tuhmc | driver_id | drivers | driver_id |
| flexsur_week | fkfsgm5n9kw7kq4gbh7trw3g9so | manual_flexsur_row_id | flexsur_manual_row | manual_flexsur_row_id |
| flexsur_week | fkk4apisv8ii6buebis7emm4jhq | shift_id | shifts | shift_id |
| flexsur_week | fkqt0vgraqfsr86cbll0cmq7cw0 | plant_id | plants | plant_id |
| flexsur_week_totals | fk3qg7p35scbn8a4pij11nywts | shift_id | shifts | shift_id |
| flexsur_week_totals | fkrm76tgfvdfvc897io8nkwt58n | plant_id | plants | plant_id |
| flexsur_week_totals_by_day | fkdxvns70ixgl1w0upi6yrie60k | flexsur_week_totals_id | flexsur_week_totals | flexsur_week_totals_id |
| format_turn_config | fkqj3cxudeyar6261g7trcpk7s2 | format_type_id | format_type | format_type_id |
| format_type_te_rule | fk9h4m6n6g4dxd9asy70hyluvqk | format_type_id | format_type | format_type_id |
| format_week | fk4y8u0c3et1xpcupmjf1x6agoe | plant_id | plants | plant_id |
| format_week | fk64hv0xtyapdqct778c9qtskp6 | format_type_id | format_type | format_type_id |
| format_week | fk8lkst19f11wsen4ws8vk024vc | route_id | routes | route_id |
| format_week | fkcrb2sava52dwo05q4267bvj5k | shift_id | shifts | shift_id |
| format_week | fkhq8velfk8h9579d1k0n53xp7 | driver_id | drivers | driver_id |
| format_week | fkhsfsby13d5fsvuk06k5ajkgd1 | manual_row_id | format_week_manual_row | manual_row_id |
| format_week_cell | fknal9mx3a8angxbqhhuwsl4xy7 | format_week_id | format_week | format_week_id |
| format_week_cell | fkq6fynymuyvycvxsj326epkkk2 | turn_config_id | format_turn_config | turn_config_id |
| format_week_manual_row | fke7dkpkqol46lph3abn6gat629 | plant_id | plants | plant_id |
| format_week_manual_row | fkiovok92po5ir392ptkt4yus21 | format_type_id | format_type | format_type_id |
| format_week_totals | fkfgc3j3r69nxvo3lob981bwiae | shift_id | shifts | shift_id |
| format_week_totals | fkjv5i1fjp0b2i4fl7qqcajmh2a | plant_id | plants | plant_id |
| format_week_totals | fkpxa6blyr40m95oe8t63sdkvmg | format_type_id | format_type | format_type_id |
| format_week_totals_unit_type | fkgmkljc7iue9blb6p47tpx42hn | format_week_totals_id | format_week_totals | format_week_totals_id |
| inbox_message_user_state | fkag29ft5te89leuh1ltu29ferb | user_id | users | user_id |
| passenger_groups | fk6litf5fnqtv11ot332oo6nvq8 | plant_id | plants | plant_id |
| plants | fkdjlj0fjh35i71duvg842mt26e | company_id | companies | company_id |
| regal_detail | fk8ip0tog9mpsycwust7wvl251t | regal_week_id | regal_week | regal_week_id |
| regal_detail | fknumbx7g4fa9jpgdjh17ypu2oi | trip_type_id | regal_trip_type | trip_type_id |
| regal_manual_row | fkp777u0k1sbbs8mnnxaexqpvkg | plant_id | plants | plant_id |
| regal_trip_type | fk6ks8jxfxjvtccnuaoub2gjetg | plant_id | plants | plant_id |
| regal_trip_type_days | fkgnsdii9f171jq2wym1i7bvt7q | trip_type_id | regal_trip_type | trip_type_id |
| regal_week | fki6h65hbv2k2yoxeo650ubfetd | driver_id | drivers | driver_id |
| regal_week | fki884bvawgp49fc29vs9cxiwf3 | plant_id | plants | plant_id |
| regal_week | fkkgq5b26ypkic7y4ow45r1vbjp | manual_regal_row_id | regal_manual_row | manual_regal_row_id |
| regal_week | fkp0u5y1713anu6dcggqfo48o18 | shift_id | shifts | shift_id |
| regal_week_summary | fkp90fmjdl17kterlqedt6xt34r | plant_id | plants | plant_id |
| role_companies | fk9guv3mkufxvd9v73dx87gqfft | company_id | companies | company_id |
| role_companies | fk9p8slonegagi5df531mtjkuxa | role_id | roles | role_id |
| role_permissions | fkegdk29eiy7mdtefy5c7eirr6e | permission_id | permissions | permission_id |
| role_permissions | fkn5fotdgk8d1xvo8nav9uv3muc | role_id | roles | role_id |
| role_plants | fkjfu385bfxpex1j0os8ok926ut | role_id | roles | role_id |
| role_plants | fkof9k0kkl0bes0lf5fpt7vsgd4 | plant_id | plants | plant_id |
| routes | fk58o28hsn23j7k8c296s93nqby | plant_id | plants | plant_id |
| shift_days | fk7me29ky1k40skvu5nn65e8vhy | shift_id | shifts | shift_id |
| shift_format_turn_map | fkj0db16p9tu1rhoo9a9jjxat25 | format_type_id | format_type | format_type_id |
| shift_format_turn_map | fknu3t2fn3bdsnbiwqfeg35d0e4 | plant_id | plants | plant_id |
| shift_format_turn_map | fkt3vefmsx0siml96uh4wqvtu0g | shift_id | shifts | shift_id |
| shift_long_week_days | fkk256ehofk5d30nuqujfhkstm5 | shift_id | shifts | shift_id |
| shift_short_week_days | fkqt3hmfpct7oyqyvv0nchgxxob | shift_id | shifts | shift_id |
| shift_wialon_aliases | fkav7jcu9q2vg04sbxvp9cwea6f | shift_id | shifts | shift_id |
| shifts | fka3jjmsxeys8tgy4pcky720fgd | plant_id | plants | plant_id |
| units | fkbkw5jlt92hq4pvyxwsiasa5qf | plant_id | plants | plant_id |
| users | fkp56c1712k691lhsyewcssf40f | role_id | roles | role_id |
| users_companies | fk4w2qm4jm4t2hjhwqdm2whap4u | company_id | companies | company_id |
| users_companies | fkdf1vge981x4u3cfob8iu8k7d7 | user_id | users | user_id |
| users_plants | fk6x06plhm8bnwsagahd1p937ay | plant_id | plants | plant_id |
| users_plants | fkdwipchrj3cjas09lg2e8cb88h | user_id | users | user_id |  

## Unique constraints
| table_name | constraint_name | column_name |
|---|---|---|
| boarding_events | uk_boarding_event_wialon_row_key | wialon_row_key |
| cascada_standard_cell | ukbsi68vvdbd56sapthvg68f3it | cascada_standard_week_id |
| cascada_standard_cell | ukbsi68vvdbd56sapthvg68f3it | day_key |
| cascada_standard_cell | ukbsi68vvdbd56sapthvg68f3it | driver_id |
| cascada_standard_cell | ukbsi68vvdbd56sapthvg68f3it | route_id |
| cascada_standard_cell | ukbsi68vvdbd56sapthvg68f3it | manual_standard_row_id |
| cascada_standard_week | uk819gbgmkvcr6a507usgrppafb | plant_id |
| cascada_standard_week | uk819gbgmkvcr6a507usgrppafb | week_start_date |
| cascada_standard_week | uk819gbgmkvcr6a507usgrppafb | shift_id |
| driver_plant_assignments | ukqemiymdrv82dx1p3ct1fwmm1q | driver_id |
| driver_plant_assignments | ukqemiymdrv82dx1p3ct1fwmm1q | plant_id |
| flexsur_driver_assignment | uk9im0pnc9vp5roie59wj9efxeq | plant_id |
| flexsur_driver_assignment | uk9im0pnc9vp5roie59wj9efxeq | driver_id |
| flexsur_driver_assignment | uk9im0pnc9vp5roie59wj9efxeq | service_id |
| flexsur_driver_assignment | uk9im0pnc9vp5roie59wj9efxeq | shift_id |
| flexsur_service_driver_assignment | ukj38etsgbdap2bu3coheekhs12 | plant_id |
| flexsur_service_driver_assignment | ukj38etsgbdap2bu3coheekhs12 | service_id |
| format_catalog | uk7fk4a197myuk170xe56w2020k | format_code |
| format_type_te_rule | uk_format_type_te_rule_format_day | format_type_id |
| format_type_te_rule | uk_format_type_te_rule_format_day | day_of_week |
| inbox_message_user_state | ukg9i14s4vebgkwx587wmi9a7ho | user_id |
| inbox_message_user_state | ukg9i14s4vebgkwx587wmi9a7ho | message_id |
| passenger_boarding_event_locations | uk_passenger_boarding_location_address_lat_lng | address |
| passenger_boarding_event_locations | uk_passenger_boarding_location_address_lat_lng | latitude |
| passenger_boarding_event_locations | uk_passenger_boarding_location_address_lat_lng | longitude |
| passenger_groups | uk_passenger_group_plant_wialon | plant_id |
| passenger_groups | uk_passenger_group_plant_wialon | wialon_id |
| passengers | uk436vk4q3mfkpy1bwj241x5mu | wialon_passenger_id |
| permissions | uk7lcb6glmvwlro3p2w2cewxtvd | code |
| regal_trip_type | ukdskh44ybjuwbjfk4syb3gqb8x | plant_id |
| regal_trip_type | ukdskh44ybjuwbjfk4syb3gqb8x | code |
| role_companies | ukgu25947rryf7bkn6mgnev2vbk | role_id |
| role_companies | ukgu25947rryf7bkn6mgnev2vbk | company_id |
| role_permissions | ukt43p6aampim70fxxnkid1mibj | role_id |
| role_permissions | ukt43p6aampim70fxxnkid1mibj | permission_id |
| role_plants | uksre27uux642ctpxrvkflx2dbd | role_id |
| role_plants | uksre27uux642ctpxrvkflx2dbd | plant_id |
| roles | ukhv4h8ntv1nqbxgh7c4e6tevy4 | role_key |
| routes | uk5wbdu2n764jobc3xd0addyil | plant_id |
| routes | uk5wbdu2n764jobc3xd0addyil | route_name |
| shift_format_turn_map | ukmx1t364eerfredshkfw4l04qk | plant_id |
| shift_format_turn_map | ukmx1t364eerfredshkfw4l04qk | format_type_id |
| shift_format_turn_map | ukmx1t364eerfredshkfw4l04qk | shift_id |
| shift_format_turn_map | ukmx1t364eerfredshkfw4l04qk | day_of_week |
| shift_wialon_aliases | ukeobjw98dxjoy4i5xo65gkf6x5 | shift_id |
| shift_wialon_aliases | ukeobjw98dxjoy4i5xo65gkf6x5 | normalized_alias_name |
| shifts | ukpouli9y6rb62yy0yb28umsxi7 | plant_id |
| shifts | ukpouli9y6rb62yy0yb28umsxi7 | shift_name |
| units | uk_unit_plant_wialon | plant_id |
| units | uk_unit_plant_wialon | wialon_id |
| users_companies | uk14t75t5aqeptea16qybb5qmte | user_id |
| users_companies | uk14t75t5aqeptea16qybb5qmte | company_id |
| users_plants | ukjamt11negoimnkep3ah2ehoob | user_id |
| users_plants | ukjamt11negoimnkep3ah2ehoob | plant_id |

## Índices
| tablename | indexname | indexdef |
|---|---|---|
| boarding_events | boarding_events_pkey | CREATE UNIQUE INDEX boarding_events_pkey ON public.boarding_events USING btree (boarding_event_id) |
| boarding_events | uk_boarding_event_wialon_row_key | CREATE UNIQUE INDEX uk_boarding_event_wialon_row_key ON public.boarding_events USING btree (wialon_row_key) |
| cascada_recipients | cascada_recipients_pkey | CREATE UNIQUE INDEX cascada_recipients_pkey ON public.cascada_recipients USING btree (cascada_recipient_id) |
| cascada_standard_cell | cascada_standard_cell_pkey | CREATE UNIQUE INDEX cascada_standard_cell_pkey ON public.cascada_standard_cell USING btree (cascada_standard_cell_id) |
| cascada_standard_cell | ukbsi68vvdbd56sapthvg68f3it | CREATE UNIQUE INDEX ukbsi68vvdbd56sapthvg68f3it ON public.cascada_standard_cell USING btree (cascada_standard_week_id, day_key, driver_id, route_id, manual_standard_row_id) |
| cascada_standard_manual_row | cascada_standard_manual_row_pkey | CREATE UNIQUE INDEX cascada_standard_manual_row_pkey ON public.cascada_standard_manual_row USING btree (manual_standard_row_id) |
| cascada_standard_week | cascada_standard_week_pkey | CREATE UNIQUE INDEX cascada_standard_week_pkey ON public.cascada_standard_week USING btree (cascada_standard_week_id) |
| cascada_standard_week | uk819gbgmkvcr6a507usgrppafb | CREATE UNIQUE INDEX uk819gbgmkvcr6a507usgrppafb ON public.cascada_standard_week USING btree (plant_id, week_start_date, shift_id) |
| companies | companies_pkey | CREATE UNIQUE INDEX companies_pkey ON public.companies USING btree (company_id) |
| driver_plant_assignments | driver_plant_assignments_pkey | CREATE UNIQUE INDEX driver_plant_assignments_pkey ON public.driver_plant_assignments USING btree (driver_plant_assignment_id) |
| driver_plant_assignments | ukqemiymdrv82dx1p3ct1fwmm1q | CREATE UNIQUE INDEX ukqemiymdrv82dx1p3ct1fwmm1q ON public.driver_plant_assignments USING btree (driver_id, plant_id) |
| driver_shifts | driver_shifts_pkey | CREATE UNIQUE INDEX driver_shifts_pkey ON public.driver_shifts USING btree (driver_id, shift_id) |
| drivers | drivers_pkey | CREATE UNIQUE INDEX drivers_pkey ON public.drivers USING btree (driver_id) |
| drivers_routes | drivers_routes_pkey | CREATE UNIQUE INDEX drivers_routes_pkey ON public.drivers_routes USING btree (driver_route_id) |
| flexsur_detail | flexsur_detail_pkey | CREATE UNIQUE INDEX flexsur_detail_pkey ON public.flexsur_detail USING btree (detail_id) |
| flexsur_driver_assignment | flexsur_driver_assignment_pkey | CREATE UNIQUE INDEX flexsur_driver_assignment_pkey ON public.flexsur_driver_assignment USING btree (flexsur_driver_assignment_id) |
| flexsur_driver_assignment | uk9im0pnc9vp5roie59wj9efxeq | CREATE UNIQUE INDEX uk9im0pnc9vp5roie59wj9efxeq ON public.flexsur_driver_assignment USING btree (plant_id, driver_id, service_id, shift_id) |
| flexsur_driver_assignment_days | flexsur_driver_assignment_days_pkey | CREATE UNIQUE INDEX flexsur_driver_assignment_days_pkey ON public.flexsur_driver_assignment_days USING btree (flexsur_driver_assignment_id, day_key) |
| flexsur_manual_row | flexsur_manual_row_pkey | CREATE UNIQUE INDEX flexsur_manual_row_pkey ON public.flexsur_manual_row USING btree (manual_flexsur_row_id) |
| flexsur_service | flexsur_service_pkey | CREATE UNIQUE INDEX flexsur_service_pkey ON public.flexsur_service USING btree (service_id) |
| flexsur_service_driver_assignment | flexsur_service_driver_assignment_pkey | CREATE UNIQUE INDEX flexsur_service_driver_assignment_pkey ON public.flexsur_service_driver_assignment USING btree (flexsur_service_driver_assignment_id) |
| flexsur_service_driver_assignment | ukj38etsgbdap2bu3coheekhs12 | CREATE UNIQUE INDEX ukj38etsgbdap2bu3coheekhs12 ON public.flexsur_service_driver_assignment USING btree (plant_id, service_id) |
| flexsur_week | flexsur_week_pkey | CREATE UNIQUE INDEX flexsur_week_pkey ON public.flexsur_week USING btree (flexsur_week_id) |
| flexsur_week_totals | flexsur_week_totals_pkey | CREATE UNIQUE INDEX flexsur_week_totals_pkey ON public.flexsur_week_totals USING btree (flexsur_week_totals_id) |
| flexsur_week_totals_by_day | flexsur_week_totals_by_day_pkey | CREATE UNIQUE INDEX flexsur_week_totals_by_day_pkey ON public.flexsur_week_totals_by_day USING btree (flexsur_week_totals_id, service_date) |
| format_catalog | format_catalog_pkey | CREATE UNIQUE INDEX format_catalog_pkey ON public.format_catalog USING btree (format_catalog_id) |
| format_catalog | uk7fk4a197myuk170xe56w2020k | CREATE UNIQUE INDEX uk7fk4a197myuk170xe56w2020k ON public.format_catalog USING btree (format_code) |
| format_turn_config | format_turn_config_pkey | CREATE UNIQUE INDEX format_turn_config_pkey ON public.format_turn_config USING btree (turn_config_id) |
| format_type | format_type_pkey | CREATE UNIQUE INDEX format_type_pkey ON public.format_type USING btree (format_type_id) |
| format_type_te_rule | format_type_te_rule_pkey | CREATE UNIQUE INDEX format_type_te_rule_pkey ON public.format_type_te_rule USING btree (format_type_te_rule_id) |
| format_type_te_rule | uk_format_type_te_rule_format_day | CREATE UNIQUE INDEX uk_format_type_te_rule_format_day ON public.format_type_te_rule USING btree (format_type_id, day_of_week) |
| format_week | format_week_pkey | CREATE UNIQUE INDEX format_week_pkey ON public.format_week USING btree (format_week_id) |
| format_week_cell | format_week_cell_pkey | CREATE UNIQUE INDEX format_week_cell_pkey ON public.format_week_cell USING btree (cell_id) |
| format_week_manual_row | format_week_manual_row_pkey | CREATE UNIQUE INDEX format_week_manual_row_pkey ON public.format_week_manual_row USING btree (manual_row_id) |
| format_week_totals | format_week_totals_pkey | CREATE UNIQUE INDEX format_week_totals_pkey ON public.format_week_totals USING btree (format_week_totals_id) |
| format_week_totals_unit_type | format_week_totals_unit_type_pkey | CREATE UNIQUE INDEX format_week_totals_unit_type_pkey ON public.format_week_totals_unit_type USING btree (format_week_totals_id, unit_type) |
| inbox_message_user_state | idx_inbox_state_message | CREATE INDEX idx_inbox_state_message ON public.inbox_message_user_state USING btree (message_id) |
| inbox_message_user_state | idx_inbox_state_user | CREATE INDEX idx_inbox_state_user ON public.inbox_message_user_state USING btree (user_id) |
| inbox_message_user_state | inbox_message_user_state_pkey | CREATE UNIQUE INDEX inbox_message_user_state_pkey ON public.inbox_message_user_state USING btree (id) |
| inbox_message_user_state | ukg9i14s4vebgkwx587wmi9a7ho | CREATE UNIQUE INDEX ukg9i14s4vebgkwx587wmi9a7ho ON public.inbox_message_user_state USING btree (user_id, message_id) |
| login_request | login_request_pkey | CREATE UNIQUE INDEX login_request_pkey ON public.login_request USING btree (id) |
| passenger_boarding_event_locations | passenger_boarding_event_locations_pkey | CREATE UNIQUE INDEX passenger_boarding_event_locations_pkey ON public.passenger_boarding_event_locations USING btree (location_id) |
| passenger_boarding_event_locations | uk_passenger_boarding_location_address_lat_lng | CREATE UNIQUE INDEX uk_passenger_boarding_location_address_lat_lng ON public.passenger_boarding_event_locations USING btree (address, latitude, longitude) |
| passenger_groups | passenger_groups_pkey | CREATE UNIQUE INDEX passenger_groups_pkey ON public.passenger_groups USING btree (passenger_group_id) |
| passenger_groups | uk_passenger_group_plant_wialon | CREATE UNIQUE INDEX uk_passenger_group_plant_wialon ON public.passenger_groups USING btree (plant_id, wialon_id) |
| passengers | passengers_pkey | CREATE UNIQUE INDEX passengers_pkey ON public.passengers USING btree (passenger_id) |
| passengers | uk436vk4q3mfkpy1bwj241x5mu | CREATE UNIQUE INDEX uk436vk4q3mfkpy1bwj241x5mu ON public.passengers USING btree (wialon_passenger_id) |
| permissions | permissions_pkey | CREATE UNIQUE INDEX permissions_pkey ON public.permissions USING btree (permission_id) |
| permissions | uk7lcb6glmvwlro3p2w2cewxtvd | CREATE UNIQUE INDEX uk7lcb6glmvwlro3p2w2cewxtvd ON public.permissions USING btree (code) |
| plants | plants_pkey | CREATE UNIQUE INDEX plants_pkey ON public.plants USING btree (plant_id) |
| regal_detail | regal_detail_pkey | CREATE UNIQUE INDEX regal_detail_pkey ON public.regal_detail USING btree (detail_id) |
| regal_manual_row | regal_manual_row_pkey | CREATE UNIQUE INDEX regal_manual_row_pkey ON public.regal_manual_row USING btree (manual_regal_row_id) |
| regal_trip_type | regal_trip_type_pkey | CREATE UNIQUE INDEX regal_trip_type_pkey ON public.regal_trip_type USING btree (trip_type_id) |
| regal_trip_type | ukdskh44ybjuwbjfk4syb3gqb8x | CREATE UNIQUE INDEX ukdskh44ybjuwbjfk4syb3gqb8x ON public.regal_trip_type USING btree (plant_id, code) |
| regal_trip_type_days | regal_trip_type_days_pkey | CREATE UNIQUE INDEX regal_trip_type_days_pkey ON public.regal_trip_type_days USING btree (trip_type_id, day_key) |
| regal_week | regal_week_pkey | CREATE UNIQUE INDEX regal_week_pkey ON public.regal_week USING btree (regal_week_id) |
| regal_week_summary | regal_week_summary_pkey | CREATE UNIQUE INDEX regal_week_summary_pkey ON public.regal_week_summary USING btree (regal_week_summary_id) |
| report_executions | report_executions_pkey | CREATE UNIQUE INDEX report_executions_pkey ON public.report_executions USING btree (report_execution_id) |
| role_companies | role_companies_pkey | CREATE UNIQUE INDEX role_companies_pkey ON public.role_companies USING btree (role_company_id) |
| role_companies | ukgu25947rryf7bkn6mgnev2vbk | CREATE UNIQUE INDEX ukgu25947rryf7bkn6mgnev2vbk ON public.role_companies USING btree (role_id, company_id) |
| role_permissions | role_permissions_pkey | CREATE UNIQUE INDEX role_permissions_pkey ON public.role_permissions USING btree (role_permission_id) |
| role_permissions | ukt43p6aampim70fxxnkid1mibj | CREATE UNIQUE INDEX ukt43p6aampim70fxxnkid1mibj ON public.role_permissions USING btree (role_id, permission_id) |
| role_plants | role_plants_pkey | CREATE UNIQUE INDEX role_plants_pkey ON public.role_plants USING btree (role_plant_id) |
| role_plants | uksre27uux642ctpxrvkflx2dbd | CREATE UNIQUE INDEX uksre27uux642ctpxrvkflx2dbd ON public.role_plants USING btree (role_id, plant_id) |
| roles | roles_pkey | CREATE UNIQUE INDEX roles_pkey ON public.roles USING btree (role_id) |
| roles | ukhv4h8ntv1nqbxgh7c4e6tevy4 | CREATE UNIQUE INDEX ukhv4h8ntv1nqbxgh7c4e6tevy4 ON public.roles USING btree (role_key) |
| routes | routes_pkey | CREATE UNIQUE INDEX routes_pkey ON public.routes USING btree (route_id) |
| routes | uk5wbdu2n764jobc3xd0addyil | CREATE UNIQUE INDEX uk5wbdu2n764jobc3xd0addyil ON public.routes USING btree (plant_id, route_name) |
| shift_days | shift_days_pkey | CREATE UNIQUE INDEX shift_days_pkey ON public.shift_days USING btree (shift_id, day_key) |
| shift_format_turn_map | shift_format_turn_map_pkey | CREATE UNIQUE INDEX shift_format_turn_map_pkey ON public.shift_format_turn_map USING btree (map_id) |
| shift_format_turn_map | ukmx1t364eerfredshkfw4l04qk | CREATE UNIQUE INDEX ukmx1t364eerfredshkfw4l04qk ON public.shift_format_turn_map USING btree (plant_id, format_type_id, shift_id, day_of_week) |
| shift_long_week_days | shift_long_week_days_pkey | CREATE UNIQUE INDEX shift_long_week_days_pkey ON public.shift_long_week_days USING btree (shift_id, day_key) |
| shift_short_week_days | shift_short_week_days_pkey | CREATE UNIQUE INDEX shift_short_week_days_pkey ON public.shift_short_week_days USING btree (shift_id, day_key) |
| shift_wialon_aliases | shift_wialon_aliases_pkey | CREATE UNIQUE INDEX shift_wialon_aliases_pkey ON public.shift_wialon_aliases USING btree (shift_wialon_alias_id) |
| shift_wialon_aliases | ukeobjw98dxjoy4i5xo65gkf6x5 | CREATE UNIQUE INDEX ukeobjw98dxjoy4i5xo65gkf6x5 ON public.shift_wialon_aliases USING btree (shift_id, normalized_alias_name) |
| shifts | shifts_pkey | CREATE UNIQUE INDEX shifts_pkey ON public.shifts USING btree (shift_id) |
| shifts | ukpouli9y6rb62yy0yb28umsxi7 | CREATE UNIQUE INDEX ukpouli9y6rb62yy0yb28umsxi7 ON public.shifts USING btree (plant_id, shift_name) |
| units | uk_unit_plant_wialon | CREATE UNIQUE INDEX uk_unit_plant_wialon ON public.units USING btree (plant_id, wialon_id) |
| units | units_pkey | CREATE UNIQUE INDEX units_pkey ON public.units USING btree (unit_id) |
| users | users_pkey | CREATE UNIQUE INDEX users_pkey ON public.users USING btree (user_id) |
| users_companies | uk14t75t5aqeptea16qybb5qmte | CREATE UNIQUE INDEX uk14t75t5aqeptea16qybb5qmte ON public.users_companies USING btree (user_id, company_id) |
| users_companies | users_companies_pkey | CREATE UNIQUE INDEX users_companies_pkey ON public.users_companies USING btree (id_users_companies) |
| users_plants | ukjamt11negoimnkep3ah2ehoob | CREATE UNIQUE INDEX ukjamt11negoimnkep3ah2ehoob ON public.users_plants USING btree (user_id, plant_id) |
| users_plants | users_plants_pkey | CREATE UNIQUE INDEX users_plants_pkey ON public.users_plants USING btree (user_plant_id) |
| wialon_sessions | wialon_sessions_pkey | CREATE UNIQUE INDEX wialon_sessions_pkey ON public.wialon_sessions USING btree (wialon_id) |

## Secuencias

## Estado puntual de tablas clave
| column_name  |
| ------------ |
| wialon_id    |
| created_at   |
| expires_at   |
| is_active    |
| last_used_at |
| sid          |
| token        |

## Datos de configuración clave para pasajeros
| plant_id | plant_name      | company_id | active | wialon_id | template_id | wialon_units_group_id |
| -------: | --------------- | ---------: | :----: | --------: | ----------: | --------------------: |
|        1 | PLANTA 1        |          1 |  true  |           |             |                       |
|        2 | F0              |          2 |  true  |           |             |                       |
|        3 | A1              |          2 |  true  |           |             |                       |
|        4 | U1              |          2 |  true  |           |             |                       |
|        5 | PLANTA 1        |          3 |  true  |           |             |                       |
|        6 | PLANTA 2        |          3 |  true  |           |             |                       |
|        7 | JUÁREZ          |          4 |  true  |           |             |                       |
|        8 | LAMINADORA      |          4 |  true  |           |             |                       |
|        9 | PLANTA 1        |          5 |  true  |           |             |                       |
|       10 | PLANTA 1        |          6 |  true  |           |             |                       |
|       11 | PLANTA 1        |          7 |  true  |           |             |                       |
|       12 | PLANTA 2        |          7 |  true  |           |             |                       |
|       13 | PLANTA 3        |          7 |  true  |           |             |                       |
|       14 | JUP2            |          8 |  true  |           |             |                       |
|       15 | PLANTA MX 1     |          9 |  true  |           |             |                       |
|       16 | PLANTA MX 2 Y 3 |          9 |  true  |           |             |                       |
|       17 | JARUDO          |         10 |  true  |           |             |                       |
|       18 | JUÁREZ          |         10 |  true  |           |             |                       |
|       19 | PLANTA 1        |         11 |  true  | 402168996 |           1 |             401947464 |

| unit_id | plant_id | wialon_id | name_raw                    | internal_id | route_code | route_name | is_active | last_synced_at            |
| ------: | -------: | --------: | --------------------------- | ----------- | ---------- | ---------- | :-------: | ------------------------- |
|       1 |       19 | 401944948 | FXS_ID 326                  | 326         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       2 |       19 | 401944823 | FS_ID 340                   | 340         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       3 |       19 | 401944816 | FXS_ID 302                  | 302         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       4 |       19 | 402068210 | FXS_ID 330                  | 330         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       5 |       19 | 401944959 | FXS_ID 321                  | 321         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       6 |       19 | 401944767 | FXS_ID 311                  | 311         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       7 |       19 | 401944824 | FXS_ID 306                  | 306         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       8 |       19 | 401711202 | FXS_ID 315                  | 315         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|       9 |       19 | 402077603 | GE-1_R-27 Horizontes ID 329 | 329         | R-27       | Horizontes |   false   | 2026-09-03 09:00:04.13267 |
|      10 |       19 | 402068256 | FXS_ID 332                  | 332         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      11 |       19 | 401391349 | FXS_ID 314                  | 314         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      12 |       19 | 402068329 | FXS_ID 334 EXTRA            |             |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      13 |       19 | 401466888 | FXS_ID 319                  | 319         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      14 |       19 | 402077520 | FXS_ID 337 EXTRA            |             |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      15 |       19 | 401466890 | FXS_ID 304                  | 304         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      16 |       19 | 401944734 | EXTRA ID 305                | 305         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      17 |       19 | 401944920 | FXS_ID 308                  | 308         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      18 |       19 | 401634717 | FXS_ID 301                  | 301         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      19 |       19 | 402077465 | FXS_ID 336                  | 336         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      20 |       19 | 402077444 | FXS_ID 335                  | 335         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      21 |       19 | 402077571 | FXS_ID 338                  | 338         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      22 |       19 | 401391385 | FXS_ID 312                  | 312         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      23 |       19 | 401944835 | FXS_ID 313                  | 313         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      24 |       19 | 401924227 | FXS_ID 316                  | 316         |            |            |    true   | 2026-09-03 09:00:04.13267 |
|      25 |       19 | 401944969 | BF_R-04 - COLINAS ID 320    | 320         | R-04       | COLINAS    |   false   | 2026-09-03 09:00:04.13267 |
|      26 |       19 | 401944905 | FXS_ID 317                  | 317         |            |            |    true   | 2026-09-03 09:00:04.13267 |

## Check constraints
| table_name                | constraint_name                             | definition                                                                                                                                                                  |
| ------------------------- | ------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| -                         | cardinal_number_domain_check                | `CHECK ((VALUE >= 0))`                                                                                                                                                      |
| -                         | yes_or_no_check                             | `CHECK (((VALUE)::text = ANY ((ARRAY['YES'::character varying, 'NO'::character varying])::text[])))`                                                                        |
| cascada_recipients        | cascada_recipients_cascada_type_check       | `CHECK (((cascada_type)::text = ANY ((ARRAY['STANDARD'::character varying, 'CUSTOM'::character varying])::text[])))`                                                        |
| cascada_standard_week     | cascada_standard_week_status_check          | `CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SENT'::character varying, 'DELETED'::character varying])::text[])))`                                     |
| driver_plant_assignments  | driver_plant_assignments_driver_type_check  | `CHECK (((driver_type)::text = ANY ((ARRAY['TITULAR'::character varying, 'EXTRA'::character varying])::text[])))`                                                           |
| drivers_routes            | drivers_routes_driver_type_check            | `CHECK (((driver_type)::text = ANY ((ARRAY['TITULAR'::character varying, 'EXTRA'::character varying])::text[])))`                                                           |
| flexsur_driver_assignment | flexsur_driver_assignment_driver_type_check | `CHECK (((driver_type)::text = ANY ((ARRAY['TITULAR'::character varying, 'EXTRA'::character varying])::text[])))`                                                           |
| flexsur_service           | flexsur_service_special_week_type_check     | `CHECK (((special_week_type)::text = ANY ((ARRAY['LONG'::character varying, 'SHORT'::character varying])::text[])))`                                                        |
| flexsur_week              | flexsur_week_status_check                   | `CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SENT'::character varying, 'DELETED'::character varying])::text[])))`                                     |
| format_week               | format_week_status_check                    | `CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SENT'::character varying, 'DELETED'::character varying])::text[])))`                                     |
| regal_week                | regal_week_status_check                     | `CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SENT'::character varying, 'DELETED'::character varying])::text[])))`                                     |
| report_executions         | report_executions_status_check              | `CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))` |
| shifts                    | shifts_shift_type_check                     | `CHECK (((shift_type)::text = ANY ((ARRAY['REGULAR'::character varying, 'SPECIAL'::character varying])::text[])))`                                                          |

## Conteo de registros por tabla
| table_name        | total |
| ----------------- | ----: |
| plants            |    19 |
| units             |    26 |
| boarding_events   |  2799 |
| shifts            |    50 |
| companies         |    11 |
| passengers        |   517 |
| passenger_groups  |     1 |
| report_executions |    11 |
| wialon_sessions   |    22 |

## Constraints por tabla puntual
| table_name      | constraint_name                              | constraint_type |
| --------------- | -------------------------------------------- | --------------- |
| boarding_events | boarding_events_boarding_event_id_not_null   | CHECK           |
| boarding_events | boarding_events_boarding_time_not_null       | CHECK           |
| boarding_events | boarding_events_created_at_not_null          | CHECK           |
| boarding_events | boarding_events_passenger_group_id_not_null  | CHECK           |
| boarding_events | boarding_events_passenger_id_not_null        | CHECK           |
| boarding_events | boarding_events_plant_id_not_null            | CHECK           |
| boarding_events | boarding_events_report_execution_id_not_null | CHECK           |
| boarding_events | boarding_events_unit_id_not_null             | CHECK           |
| boarding_events | boarding_events_updated_at_not_null          | CHECK           |
| boarding_events | boarding_events_wialon_row_key_not_null      | CHECK           |
| boarding_events | fkejy4vg9s2lbtbsfl1pdqxbvx3                  | FOREIGN KEY     |
| boarding_events | fkhew9von0l6wb5yfwnexo8ex7w                  | FOREIGN KEY     |
| boarding_events | fkkiex7ilg6x9vg0eck1p3eg233                  | FOREIGN KEY     |
| boarding_events | fknpjsghd1uhp6hmirc5xmxe39n                  | FOREIGN KEY     |
| boarding_events | fkrcrdat6qadn8c8vmrw7nuad4g                  | FOREIGN KEY     |
| boarding_events | boarding_events_pkey                         | PRIMARY KEY     |
| boarding_events | uk_boarding_event_wialon_row_key             | UNIQUE          |
| plants          | plants_active_not_null                       | CHECK           |
| plants          | plants_company_id_not_null                   | CHECK           |
| plants          | plants_format_catalog_id_not_null            | CHECK           |
| plants          | plants_plant_id_not_null                     | CHECK           |
| plants          | fkdjlj0fjh35i71duvg842mt26e                  | FOREIGN KEY     |
| plants          | plants_pkey                                  | PRIMARY KEY     |
| shifts          | shifts_active_not_null                       | CHECK           |
| shifts          | shifts_end_time_not_null                     | CHECK           |
| shifts          | shifts_plant_id_not_null                     | CHECK           |
| shifts          | shifts_shift_id_not_null                     | CHECK           |
| shifts          | shifts_shift_name_not_null                   | CHECK           |
| shifts          | shifts_shift_type_check                      | CHECK           |
| shifts          | shifts_start_time_not_null                   | CHECK           |
| shifts          | fka3jjmsxeys8tgy4pcky720fgd                  | FOREIGN KEY     |
| shifts          | shifts_pkey                                  | PRIMARY KEY     |
| shifts          | ukpouli9y6rb62yy0yb28umsxi7                  | UNIQUE          |
| units           | units_is_active_not_null                     | CHECK           |
| units           | units_name_raw_not_null                      | CHECK           |
| units           | units_plant_id_not_null                      | CHECK           |
| units           | units_unit_id_not_null                       | CHECK           |
| units           | units_wialon_id_not_null                     | CHECK           |
| units           | fkbkw5jlt92hq4pvyxwsiasa5qf                  | FOREIGN KEY     |
| units           | units_pkey                                   | PRIMARY KEY     |
| units           | uk_unit_plant_wialon                         | UNIQUE          |

## Foreign keys con definición completa
| table_name                        | constraint_name             | definition                                                                                                      |
| --------------------------------- | --------------------------- | --------------------------------------------------------------------------------------------------------------- |
| boarding_events                   | fkejy4vg9s2lbtbsfl1pdqxbvx3 | `FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id)`                                                |
| boarding_events                   | fkhew9von0l6wb5yfwnexo8ex7w | `FOREIGN KEY (passenger_group_id) REFERENCES passenger_groups(passenger_group_id)`                              |
| boarding_events                   | fkkiex7ilg6x9vg0eck1p3eg233 | `FOREIGN KEY (report_execution_id) REFERENCES report_executions(report_execution_id)`                           |
| boarding_events                   | fknpjsghd1uhp6hmirc5xmxe39n | `FOREIGN KEY (unit_id) REFERENCES units(unit_id)`                                                               |
| boarding_events                   | fkrcrdat6qadn8c8vmrw7nuad4g | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| cascada_recipients                | fks1na6sq7dwb058o8s01lo42tm | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| cascada_standard_cell             | fk14rup5r0f5hex9pnhytdixpd2 | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| cascada_standard_cell             | fkco16atfr33mssl0pp3fp71ase | `FOREIGN KEY (cascada_standard_week_id) REFERENCES cascada_standard_week(cascada_standard_week_id)`             |
| cascada_standard_cell             | fkkogigd2xrl1qlemjkt9bitj7d | `FOREIGN KEY (manual_standard_row_id) REFERENCES cascada_standard_manual_row(manual_standard_row_id)`           |
| cascada_standard_manual_row       | fka0g4f2y4dhap1gqdtabykpcj2 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| cascada_standard_week             | fk6sa7d4jt26jpkyk1ghdoi8gdw | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| driver_plant_assignments          | fk96tlhwj40ghgja17yd6ucjhbx | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| driver_plant_assignments          | fki92n06iy8lpy8rdt4jnkrtvt  | `FOREIGN KEY (route_id) REFERENCES routes(route_id)`                                                            |
| driver_plant_assignments          | fknrik2mdm5y613b08ule5g4xjb | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| drivers                           | fkr393c6lft794txqccr5chph3s | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| driver_shifts                     | fk3wg4dy5prbnm8wgks02syt2nh | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| driver_shifts                     | fkjpenr1v05f40j3n0vcjdsuju8 | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| drivers_routes                    | fka4xemhl5gshwwyn8ls5dujba7 | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| drivers_routes                    | fkr5c4invag7rt8o19uv1w7c2mj | `FOREIGN KEY (route_id) REFERENCES routes(route_id)`                                                            |
| flexsur_detail                    | fkmy4pcmqjjncyyq6ytnwnglbtw | `FOREIGN KEY (flexsur_week_id) REFERENCES flexsur_week(flexsur_week_id)`                                        |
| flexsur_driver_assignment         | fk1fygvpn4ukisqi0n3s6vwpjh4 | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| flexsur_driver_assignment         | fk9tgk6ey5rwuhf1y9f7xb18u9u | `FOREIGN KEY (route_id) REFERENCES routes(route_id)`                                                            |
| flexsur_driver_assignment         | fkdblvu06lcccth2riukqoagicb | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| flexsur_driver_assignment         | fkhp1f79tpn73g8bj1vg4j15g7i | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| flexsur_driver_assignment         | fkloie2g0ibhgy64e42w2iq7bxt | `FOREIGN KEY (service_id) REFERENCES flexsur_service(service_id)`                                               |
| flexsur_driver_assignment_days    | fkfkohm7xdca9de0yy8gq8wylus | `FOREIGN KEY (flexsur_driver_assignment_id) REFERENCES flexsur_driver_assignment(flexsur_driver_assignment_id)` |
| flexsur_manual_row                | fkerw5xrqc4ydl7pcw0igh4il1t | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| flexsur_manual_row                | fkr81xsbggd6f06c2s5mxnx1t8t | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| flexsur_service                   | fkdcwlavd6j64bps5y1w0ib4vr6 | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| flexsur_service                   | fkmgxiqi31f4od224e52biy05kl | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| flexsur_service_driver_assignment | fk3hgft5ipd2q1wbdfdlcv5u02v | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| flexsur_service_driver_assignment | fkghlo3hf5f5lgtu6d9e7nu6b1w | `FOREIGN KEY (service_id) REFERENCES flexsur_service(service_id)`                                               |
| flexsur_service_driver_assignment | fkijtm8qxywckpnq28elh5tuhmc | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| flexsur_week                      | fkfsgm5n9kw7kq4gbh7trw3g9so | `FOREIGN KEY (manual_flexsur_row_id) REFERENCES flexsur_manual_row(manual_flexsur_row_id)`                      |
| flexsur_week                      | fkk4apisv8ii6buebis7emm4jhq | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| flexsur_week                      | fkqt0vgraqfsr86cbll0cmq7cw0 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| flexsur_week_totals               | fk3qg7p35scbn8a4pij11nywts  | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| flexsur_week_totals               | fkrm76tgfvdfvc897io8nkwt58n | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| flexsur_week_totals_by_day        | fkdxvns70ixgl1w0upi6yrie60k | `FOREIGN KEY (flexsur_week_totals_id) REFERENCES flexsur_week_totals(flexsur_week_totals_id)`                   |
| format_turn_config                | fkqj3cxudeyar6261g7trcpk7s2 | `FOREIGN KEY (format_type_id) REFERENCES format_type(format_type_id)`                                           |
| format_type_te_rule               | fk9h4m6n6g4dxd9asy70hyluvqk | `FOREIGN KEY (format_type_id) REFERENCES format_type(format_type_id)`                                           |
| format_week                       | fk4y8u0c3et1xpcupmjf1x6agoe | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| format_week                       | fk64hv0xtyapdqct778c9qtskp6 | `FOREIGN KEY (format_type_id) REFERENCES format_type(format_type_id)`                                           |
| format_week                       | fk8lkst19f11wsen4ws8vk024vc | `FOREIGN KEY (route_id) REFERENCES routes(route_id)`                                                            |
| format_week                       | fkcrb2sava52dwo05q4267bvj5k | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| format_week                       | fkhq8velfk8h9579d1k0n53xp7  | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| format_week                       | fkhsfsby13d5fsvuk06k5ajkgd1 | `FOREIGN KEY (manual_row_id) REFERENCES format_week_manual_row(manual_row_id)`                                  |
| format_week_cell                  | fknal9mx3a8angxbqhhuwsl4xy7 | `FOREIGN KEY (format_week_id) REFERENCES format_week(format_week_id)`                                           |
| format_week_cell                  | fkq6fynymuyvycvxsj326epkkk2 | `FOREIGN KEY (turn_config_id) REFERENCES format_turn_config(turn_config_id)`                                    |
| format_week_manual_row            | fke7dkpkqol46lph3abn6gat629 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| format_week_manual_row            | fkiovok92po5ir392ptkt4yus21 | `FOREIGN KEY (format_type_id) REFERENCES format_type(format_type_id)`                                           |
| format_week_totals                | fkfgc3j3r69nxvo3lob981bwiae | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| format_week_totals                | fkjv5i1fjp0b2i4fl7qqcajmh2a | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| format_week_totals                | fkpxa6blyr40m95oe8t63sdkvmg | `FOREIGN KEY (format_type_id) REFERENCES format_type(format_type_id)`                                           |
| format_week_totals_unit_type      | fkgmkljc7iue9blb6p47tpx42hn | `FOREIGN KEY (format_week_totals_id) REFERENCES format_week_totals(format_week_totals_id)`                      |
| inbox_message_user_state          | fkag29ft5te89leuh1ltu29ferb | `FOREIGN KEY (user_id) REFERENCES users(user_id)`                                                               |
| passenger_groups                  | fk6litf5fnqtv11ot332oo6nvq8 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| plants                            | fkdjlj0fjh35i71duvg842mt26e | `FOREIGN KEY (company_id) REFERENCES companies(company_id)`                                                     |
| regal_detail                      | fk8ip0tog9mpsycwust7wvl251t | `FOREIGN KEY (regal_week_id) REFERENCES regal_week(regal_week_id)`                                              |
| regal_detail                      | fknumbx7g4fa9jpgdjh17ypu2oi | `FOREIGN KEY (trip_type_id) REFERENCES regal_trip_type(trip_type_id)`                                           |
| regal_manual_row                  | fkp777u0k1sbbs8mnnxaexqpvkg | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| regal_trip_type                   | fk6ks8jxfxjvtccnuaoub2gjetg | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| regal_trip_type_days              | fkgnsdii9f171jq2wym1i7bvt7q | `FOREIGN KEY (trip_type_id) REFERENCES regal_trip_type(trip_type_id)`                                           |
| regal_week                        | fki6h65hbv2k2yoxeo650ubfetd | `FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)`                                                         |
| regal_week                        | fki884bvawgp49fc29vs9cxiwf3 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| regal_week                        | fkkgq5b26ypkic7y4ow45r1vbjp | `FOREIGN KEY (manual_regal_row_id) REFERENCES regal_manual_row(manual_regal_row_id)`                            |
| regal_week                        | fkp0u5y1713anu6dcggqfo48o18 | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| regal_week_summary                | fkp90fmjdl17kterlqedt6xt34r | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| role_companies                    | fk9guv3mkufxvd9v73dx87gqfft | `FOREIGN KEY (company_id) REFERENCES companies(company_id)`                                                     |
| role_companies                    | fk9p8slonegagi5df531mtjkuxa | `FOREIGN KEY (role_id) REFERENCES roles(role_id)`                                                               |
| role_permissions                  | fkegdk29eiy7mdtefy5c7eirr6e | `FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)`                                             |
| role_permissions                  | fkn5fotdgk8d1xvo8nav9uv3muc | `FOREIGN KEY (role_id) REFERENCES roles(role_id)`                                                               |
| role_plants                       | fkjfu385bfxpex1j0os8ok926ut | `FOREIGN KEY (role_id) REFERENCES roles(role_id)`                                                               |
| role_plants                       | fkof9k0kkl0bes0lf5fpt7vsgd4 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| routes                            | fk58o28hsn23j7k8c296s93nqby | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| shift_days                        | fk7me29ky1k40skvu5nn65e8vhy | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| shift_format_turn_map             | fkj0db16p9tu1rhoo9a9jjxat25 | `FOREIGN KEY (format_type_id) REFERENCES format_type(format_type_id)`                                           |
| shift_format_turn_map             | fknu3t2fn3bdsnbiwqfeg35d0e4 | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| shift_format_turn_map             | fkt3vefmsx0siml96uh4wqvtu0g | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| shift_long_week_days              | fkk256ehofk5d30nuqujfhkstm5 | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| shifts                            | fka3jjmsxeys8tgy4pcky720fgd | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| shift_short_week_days             | fkqt3hmfpct7oyqyvv0nchgxxob | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| shift_wialon_aliases              | fkav7jcu9q2vg04sbxvp9cwea6f | `FOREIGN KEY (shift_id) REFERENCES shifts(shift_id)`                                                            |
| units                             | fkbkw5jlt92hq4pvyxwsiasa5qf | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| users                             | fkp56c1712k691lhsyewcssf40f | `FOREIGN KEY (role_id) REFERENCES roles(role_id)`                                                               |
| users_companies                   | fk4w2qm4jm4t2hjhwqdm2whap4u | `FOREIGN KEY (company_id) REFERENCES companies(company_id)`                                                     |
| users_companies                   | fkdf1vge981x4u3cfob8iu8k7d7 | `FOREIGN KEY (user_id) REFERENCES users(user_id)`                                                               |
| users_plants                      | fk6x06plhm8bnwsagahd1p937ay | `FOREIGN KEY (plant_id) REFERENCES plants(plant_id)`                                                            |
| users_plants                      | fkdwipchrj3cjas09lg2e8cb88h | `FOREIGN KEY (user_id) REFERENCES users(user_id)`                                                               |

## Índices solo de tablas clave
| tablename       | indexname                        | indexdef                                                                                                      |
| --------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| boarding_events | boarding_events_pkey             | `CREATE UNIQUE INDEX boarding_events_pkey ON public.boarding_events USING btree (boarding_event_id)`          |
| boarding_events | uk_boarding_event_wialon_row_key | `CREATE UNIQUE INDEX uk_boarding_event_wialon_row_key ON public.boarding_events USING btree (wialon_row_key)` |
| plants          | plants_pkey                      | `CREATE UNIQUE INDEX plants_pkey ON public.plants USING btree (plant_id)`                                     |
| shifts          | shifts_pkey                      | `CREATE UNIQUE INDEX shifts_pkey ON public.shifts USING btree (shift_id)`                                     |
| shifts          | ukpouli9y6rb62yy0yb28umsxi7      | `CREATE UNIQUE INDEX ukpouli9y6rb62yy0yb28umsxi7 ON public.shifts USING btree (plant_id, shift_name)`         |
| units           | uk_unit_plant_wialon             | `CREATE UNIQUE INDEX uk_unit_plant_wialon ON public.units USING btree (plant_id, wialon_id)`                  |
| units           | units_pkey                       | `CREATE UNIQUE INDEX units_pkey ON public.units USING btree (unit_id)`                                        |

## Observaciones clave

- La base de datos de producción inspeccionada el 2 de septiembre de 2026 corresponde a un esquema compartido por dos
  módulos:
    - cascada de viajes
    - monitoreo de pasajeros

- La tabla `plants` ya existía previamente al desarrollo del módulo de monitoreo de pasajeros y continúa siendo una
  tabla compartida entre ambos módulos.

- La tabla `shifts` también forma parte del esquema compartido y es utilizada por ambos módulos.

- Las tablas principales del módulo de monitoreo de pasajeros actualmente identificadas en producción son:
    - `boarding_events`
    - `passengers`
    - `passenger_groups`
    - `units`
    - `report_executions`
    - `wialon_sessions`

- Al momento de esta inspección, en la base de datos de producción las tablas `plants` y `units` aún conservan la
  columna `wialon_id`.

- En el código backend ya existe un refactor de naming para expresar mejor el propósito de esos campos:
    - `plants.wialon_resource_id`
    - `units.wialon_unit_id`

- Ese refactor de nombres todavía no ha sido aplicado físicamente en la base de datos de producción, por lo que el
  estado real actual del esquema sigue usando:
    - `plants.wialon_id`
    - `units.wialon_id`

- La tabla `units` en producción conserva actualmente la restricción e índice asociados al naming anterior:
    - `uk_unit_plant_wialon`
    - `units_wialon_id_not_null`

- Existen cambios históricos aplicados manualmente en producción antes de integrar Flyway, por lo que el esquema
  actual debe considerarse una base viva previa, no un esquema generado enteramente por migraciones versionadas.

## Decisión de baseline

El baseline de Flyway deberá realizarse tomando como fuente de verdad el estado real actual de la base de datos de
producción inspeccionada el 2 de septiembre de 2026.

Esto implica que:
0
- el baseline no debe asumir todavía los renombres físicos de columnas pendientes en producción
- el baseline debe representar el esquema compartido completo, no únicamente las tablas del módulo de monitoreo de
  pasajeros
- los cambios ya reflejados en código pero no aplicados aún en producción deberán tratarse como migraciones
  posteriores al baseline

A partir de este baseline:

- toda modificación nueva al esquema deberá gestionarse mediante migraciones versionadas con Flyway
- los renombres pendientes de `wialon_id` en `plants` y `units` deberán incorporarse como migraciones posteriores
- se deberá evitar continuar aplicando cambios manuales directos en producción salvo en escenarios excepcionales y
  controlados
