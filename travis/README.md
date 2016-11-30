Travis-CI Builds and Actions
============================

|                    | PRs                | Branch             | Master             | Whitelist          | Tag                |
|--------------------|--------------------|--------------------|--------------------|--------------------|--------------------|
| What Builds?       | Merge src & dest   | Branch head        | Master head        | Branch head        | Tag commit         |
| Check dependencies | :white_check_mark: | :white_check_mark: | :white_check_mark: | :x:                | :x:                |
| Update Version Eye | :x:                | :x:                | :white_check_mark: | :x:                | :x:                |
| Set version        | :x:                | :x:                | :x:                | :x:                | :white_check_mark: |
| Create tag         | :x:                | :x:                | :white_check_mark: | :white_check_mark: | :x:                |
| Publish            | :x:                | :x:                | :x:                | :x:                | :white_check_mark: |

Build Details
-------------

Fili practices Continuous Integration and Continuous Deployment, releasing early and often to get changes out quickly.
To do this we rely more on build scripting, and less on Travis' built-in build support. These build scripts make it hard
to understand what Travis is doing and when, so here is what should be happening for different types of Fili builds. 

| Build Types                                           | Build Actions                                       |
|-------------------------------------------------------|-----------------------------------------------------|
| [Pull Requests](#pull-requests)                       | Something get's built                               |
| [Branch](#branch)                                     | Run license and version checks                      |
| [Master](#master)                                     | Update Version Eye                                  |
| [Other Whitelisted Branch](#other-whitelisted-branch) | Set version (ie. change SNAPSHOT to a real version) |
| [Version Tag](#version-tag)                           | Create tag (ie. version bump)                       |
|                                                       | Push to Bintray (ie. release)                       |

Build Flows
-----------

Here is what should be going on for the 5 different kinds of Fili builds that Travis is handling:

### Pull Requests

Proposed changes, triggered when a PR is opened or a PR's source branch moves.

1. Build the merge of the source branch into the destination branch
2. Run license and version checks

### Branch

Potential changes, triggered when a branch is created or moves.

1. Build the head of the branch
2. Run license and version checks

### Master

Bleeding edge, triggered when the `master` branch moves.

1. Build the head of the branch
2. Run license and version checks
3. Update Version Eye
4. Create tag

### Other Whitelisted Branch

Special supported version, triggered when the branch moves.

1. Build the head of the the branch
2. Create tag

### Version Tag

Version to release, triggered when a version tag is created or moved.

1. Build the commit at the tag
2. Push to Bintray
