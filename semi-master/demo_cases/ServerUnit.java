public class ServerUnit extends Unit implements ServerModelObject {
	
	public Resource[][] grabManifests(Resource[] rcs) {
		Resource[][] manifests = new Resource[rcs.length][] ;
		for(int i=0; i<rcs.length; i++) {
			Resource[][] rec = null;
			if(rcs[i] instanceof FileSet) { 
			  rec = grabRes(new FileSet[] {(FileSet)rcs[i]});
			} else {
			  rec = grabNonFileSetRes(new Resource []{ rcs[i] });
			}
			for(int j=0; j< rec[0].length; j++) {
				String name = rec[0][j].getName().replace('\\','/');
				if(rcs[i] instanceof ArchiveFileSet) {
					 ArchiveFileSet afs = (ArchiveFileSet) rcs[i];
					 if (!"".equals(afs.getFullpath(getProj()))) {
						name.afs.getFullpath(getProj());
					 } else if(!"".equals(afs.getPref(getProj()))) {
						String pr = afs.getPref(getProj());
						if(!pr.endsWith("/") &&  !pr.endsWith("\\")) {
						   pr += "/";
						}
						name = pr + name;
					 } 
				}
				if (name.equalsIgnoreCase(MANIFEST_NAME)) {
					manifests[i] = new Resource[] {rec[0][j]};
					break;
				}
			}
			if (manifests[i] == null) {
				manifests[i] = new Resource[0];
			}
		}
		return manifests;
	}

    private static final Logger logger = Logger.getLogger(ServerUnit.class.getName());

    		
    /**
     * Completes a tile improvement.
     *
     * @param random A pseudo-random number source.
     * @param cs A <code>ChangeSet</code> to update.
     */
    private void csImproveTile(Random random, ChangeSet cs) {
        Tile tile = getTile();
        AbstractGoods deliver = getWorkImprovement().getType().getProduction(tile.getType());
        if (deliver != null) { // Deliver goods if any
            int amount = deliver.getAmount();
            if (getType().hasAbility(Ability.EXPERT_PIONEER)) {
                amount *= 2;
            }
            Settlement settlement = tile.getSettlement();
            if (settlement != null
                && (ServerPlayer) settlement.getOwner() == owner) {
                amount = (int)settlement.applyModifier(amount,
                    Modifier.TILE_TYPE_CHANGE_PRODUCTION, deliver.getType());
                settlement.addGoods(deliver.getType(), amount);
            } else {
                List<Settlement> adjacent = new ArrayList<Settlement>();
                int newAmount = amount;
                for (Tile t : tile.getSurroundingTiles(2)) {
                    Settlement ts = t.getSettlement();
                    if (ts != null && (ServerPlayer)ts.getOwner() == owner) {
                        adjacent.add(ts);
                        int modAmount = (int)ts.applyModifier((float)amount, Modifier.TILE_TYPE_CHANGE_PRODUCTION, deliver.getType());
                        if (modAmount > newAmount) {
                            newAmount = modAmount;
                        }
                    }
                }
                if (adjacent.size() > 0) {
                    int deliverPerCity = newAmount / adjacent.size();
                    for (Settlement s : adjacent) {
                        s.addGoods(deliver.getType(), deliverPerCity);
                    }
                    // Add residue to first adjacent settlement.
                    adjacent.get(0).addGoods(deliver.getType(), newAmount % adjacent.size());
                }
            }
        }

        // Finish up
        TileImprovement ti = getWorkImprovement();
        TileType changeType = ti.getChange(tile.getType());
        if (changeType != null) {
            // Changes like clearing a forest need to be completed,
            // whereas for changes like road building the improvement
            // is already added and now complete.
            tile.setType(changeType);
        }

        // Does a resource get exposed?
        TileImprovementType tileImprovementType = ti.getType();
        int exposeResource = tileImprovementType.getExposeResourcePercent();
        if (exposeResource > 0 && !tile.hasResource()) {
            if (Utils.randomInt(logger, "Expose resource", random, 100)
                < exposeResource) {
                ResourceType resType = RandomChoice.getWeightedRandom(logger, "Resource type", random, tile.getType().getWeightedResources());
                int minValue = resType.getMinValue();
                int maxValue = resType.getMaxValue();
                int value = minValue + ((minValue == maxValue) ? 0 : Utils.randomInt(logger, "Resource quantity", random, maxValue - minValue + 1));
                tile.addResource(new Resource(getGame(), tile, resType, value));
            }
        }

        // Expend equipment
        EquipmentType type = ti.getExpendedEquipmentType();
        changeEquipment(type, -ti.getExpendedAmount());
        for (Unit unit : tile.getUnitList()) {
            if (unit.getWorkImprovement() != null
                && unit.getWorkImprovement().getType() == ti.getType()
                && unit.getState() == UnitState.IMPROVING) {
                unit.setWorkLeft(-1);
                unit.setWorkImprovement(null);
                unit.setState(UnitState.ACTIVE);
                unit.setMovesLeft(0);
            }
        }
        // TODO: make this more generic, currently assumes tools used
        EquipmentType tools = getSpecification()
            .getEquipmentType("model.equipment.tools");
        if (type == tools && getEquipmentCount(tools) == 0) {
            ServerPlayer owner = (ServerPlayer) getOwner();
            StringTemplate locName
                = getLocation().getLocationNameFor(owner);
            String messageId = (getType().getDefaultEquipmentType() == type)
                ? getType() + ".noMoreTools"
                : "model.unit.noMoreTools";
            cs.addMessage(See.only(owner),
                new ModelMessage(ModelMessage.MessageType.WARNING,
                    messageId, this)
                .addStringTemplate("%unit%", getLabel())
                .addStringTemplate("%location%", locName));
        }
    }
	
	private void internalDefocusComponent(Component component) { // UNDER FOCUSED_LOCK
	// TODO: Нигде не проверяется на идентияность next и component!!!          
        Container container = component.getContainer();
        Focusable next;
        if (container != null && !container.getInternal().isRemovingFromHierarchy()) {
            next = container.getChildren().getInternal().getTopAvailableForm(component);
            if (next != null) {
                setFocused(next, true, true);
                return;
            }
        }
        do {
            container = component.getParentForm();
            if (container == null)
                container = this;
            if (!container.getInternal().isRemovingFromHierarchy()) {
                next = container.getChildren().getInternal().getTopAvailableForm(component);
                if (next == null)
                    next = container.getChildren().getInternal().getNextTabStopTabable(component);
                if (next == null && container instanceof Focusable && ((Focusable)container).canFocus())
                    next = (Focusable)container;
                if (next != null) {
                    if (next instanceof Form)
                        ((Form)next).bringToFront();
                    else
                        setFocused(next, true, true);
                    return;
                }
            }
            component = container instanceof Component ? (Component)container : null;
        } while (component != null);

        setFocused(null);
    }
	
    public void deactivateContainer(Container container) {
        if (container == null)
            return;
        final ReentrantLock lock = this.FOCUSED_LOCK;
        lock.lock();
        try {
            Focusable focused = getFocused();
            if (container != focused && !container.isElementInHierarchy(focused))
                return;

            if (container instanceof Component)
                internalDefocusComponent((Component)container);
            else
                setFocused(null);
        } finally {
            lock.unlock();
        }
    }
	
	public void load(Class clazz) throws ResourceNotFoundException, IOException {
        String resource = Loader.getResource(clazz);
        InputStream in = Resources.getResourceAsStream(resource, getInstance().getServletContext());
        if (in == null)
            throw new ResourceNotFoundException(resource);
        Loader.getInstance().load(this, in);
    }
	
	public void IamJustAsmallDummyOne(){
		int x =0;
		x+=3;
		System.out.println(x);
	}

	private void csImprove(Random random, ChangeSet cs) {
        Tile tile = getTile();
        AbstractGoods deliver = getWorkImprovement().getType().getProduction(tile.getType());
        if (deliver != null) { // Deliver goods if any
            int amount = deliver.getAmount();
            if (getType().hasAbility(Ability.EXPERT_PIONEER)) {
                amount *= 2;
            }
            Settlement settlement = tile.getSettlement();
            if (settlement != null
                && (ServerPlayer) settlement.getOwner() == owner) {
                amount = (int)settlement.applyModifier(amount,
                    Modifier.TILE_TYPE_CHANGE_PRODUCTION, deliver.getType());
                settlement.addGoods(deliver.getType(), amount);
            } else {
                List<Settlement> adjacent = new ArrayList<Settlement>();
                int newAmount = amount;
                for (Tile t : tile.getSurroundingTiles(2)) {
                    Settlement ts = t.getSettlement();
                    if (ts != null && (ServerPlayer)ts.getOwner() == owner) {
                        adjacent.add(ts);
                        int modAmount = (int)ts.applyModifier((float)amount,
                            Modifier.TILE_TYPE_CHANGE_PRODUCTION,
                            deliver.getType());
                        if (modAmount > newAmount) {
                            newAmount = modAmount;
                        }
                    }
                }
                if (adjacent.size() > 0) {
                    int deliverPerCity = newAmount / adjacent.size();
                    for (Settlement s : adjacent) {
                        s.addGoods(deliver.getType(), deliverPerCity);
                    }
                    // Add residue to first adjacent settlement.
                    adjacent.get(0).addGoods(deliver.getType(),
                                             newAmount % adjacent.size());
                }
            }
        }

        // Finish up
        TileImprovement ti = getWorkImprovement();
        TileType changeType = ti.getChange(tile.getType());
        if (changeType != null) {
            // Changes like clearing a forest need to be completed,
            // whereas for changes like road building the improvement
            // is already added and now complete.
            tile.setType(changeType);
        }

        // Does a resource get exposed?
        TileImprovementType tileImprovementType = ti.getType();
        int exposeResource = tileImprovementType.getExposeResourcePercent();
        if (exposeResource > 0 && !tile.hasResource()) {
            if (Utils.randomInt(logger, "Expose resource", random, 100)
                < exposeResource) {
                ResourceType resType = RandomChoice.getWeightedRandom(logger,
                                                                      "Resource type", random,
                                                                      tile.getType().getWeightedResources());
                int minValue = resType.getMinValue();
                int maxValue = resType.getMaxValue();
                int value = minValue + ((minValue == maxValue) ? 0
                                        : Utils.randomInt(logger, "Resource quantity",
                                                          random, maxValue - minValue + 1));
                tile.addResource(new Resource(getGame(), tile, resType, value));
            }
        }

        // Expend equipment
        EquipmentType type = ti.getExpendedEquipmentType();
        changeEquipment(type, -ti.getExpendedAmount());
        for (Unit unit : tile.getUnitList()) {
            if (unit.getWorkImprovement() != null
                && unit.getWorkImprovement().getType() == ti.getType()
                && unit.getState() == UnitState.IMPROVING) {
                unit.setWorkLeft(-1);
                unit.setWorkImprovement(null);
                unit.setState(UnitState.ACTIVE);
                unit.setMovesLeft(0);
            }
        }
    }
}
