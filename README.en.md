Chinese / [English](README.en.md)

## about

neatlogic-itsm is an it service management module, which comes with workflow engine, custom form, service catalog, SLA
management and other functions.

## Feature

### Workflow

It comes with built-in nodes such as general, condition, and timing. If other functional modules are activated, special
nodes such as automation, configuration item synchronization, and change can also be used.
Support serial, parallel, conditional, aggregation, shunt, fallback and other flow modes.
Supports the implementation of custom nodes through extensions.
![img.png](README_IMAGES/img.png)

- Support calling third-party interfaces, and the process provides automatic processing nodes for calling third-party
  interfaces in the process.
- Supports webhook triggers, work orders or a node in a specified state, triggering calls to third-party interface
  actions.
- Support automatic start and transfer of nodes.
- Supports complex work order assignment logic, including assignment to personnel, organizations, roles, stakeholders,
  pre-step processor assignments, or assignments based on form values.

### SLA

Policy-based SLA calculation method.
![img.png](README_IMAGES/img1.png)

- Supports time-limited policies that are accurate to work orders.
- Supports time-limited policies that are accurate to nodes.
- Support setting dynamic time limit based on priority, scope of influence, whether the reporter is a VIP, etc.
- Support task timeout or temporary notification.
- Support task timeout or temporary automatic assignment.

## service directory

![img.png](README_IMAGES/img3.png)

- Support custom service catalog and hierarchical relationship.
- Support custom service priority, service category, service reporting authority, etc.

## Work Order Center

![img.png](README_IMAGES/img2.png)
The work order center allows users to preset various search criteria according to personal roles, so as to facilitate
tracking of various work orders.