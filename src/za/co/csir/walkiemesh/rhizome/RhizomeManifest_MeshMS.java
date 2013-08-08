/**
 * Copyright (C) 2011 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package za.co.csir.walkiemesh.rhizome;

import java.io.File;
import java.io.IOException;

import za.co.csir.walkiemesh.servald.SubscriberId;

import android.os.Bundle;

/**
 * Represents a Rhizome MeshMS manifest, with methods to serialise to/from a byte stream for storage
 * on disk.
 *
 * @author Andrew Bettison <andrew@servalproject.com>
 */
public class RhizomeManifest_MeshMS extends RhizomeManifest {

	public final static String SERVICE = "MeshMS1";

	private SubscriberId mSender;
	private SubscriberId mRecipient;

	/** Construct a Rhizome MeshMS manifest from its byte-stream representation.
	 *
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public static RhizomeManifest_MeshMS fromByteArray(byte[] bytes) throws RhizomeManifestParseException {
		RhizomeManifest manifest = RhizomeManifest.fromByteArray(bytes);
		if (!(manifest instanceof RhizomeManifest_MeshMS))
			throw new RhizomeManifestParseException("not a MeshMS manifest, service='" + manifest.getService() + "'");
		return (RhizomeManifest_MeshMS) manifest;
	}

	/** Helper function to read a MeshMS manifest from a file.
	 *
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public static RhizomeManifest_MeshMS readFromFile(File manifestFile)
		throws IOException, RhizomeManifestSizeException, RhizomeManifestParseException, RhizomeManifestServiceException
	{
		RhizomeManifest man = RhizomeManifest.readFromFile(manifestFile);
		try {
			return (RhizomeManifest_MeshMS) man;
		}
		catch (ClassCastException e) {
			throw new RhizomeManifestServiceException(SERVICE, man.getService());
		}
	}

	/** Construct a Rhizome MeshMS manifest from a bundle of named fields.
	 *
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public static RhizomeManifest_MeshMS fromBundle(Bundle b, byte[] signatureBlock) throws RhizomeManifestParseException {
		String service = parseNonEmpty("service", b.getString("service"));
		if (service == null)
			throw new RhizomeManifestParseException("missing 'service' field");
		if (!service.equalsIgnoreCase(SERVICE))
			throw new RhizomeManifestParseException("mismatched service '" + service + "'");
		return new RhizomeManifest_MeshMS(b, signatureBlock);
	}

	@Override
	public RhizomeManifest_MeshMS clone() throws CloneNotSupportedException {
		return (RhizomeManifest_MeshMS) super.clone();
	}

	/** Construct an empty Rhizome MeshMS manifest.
	 *
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	protected RhizomeManifest_MeshMS() {
		mSender = null;
		mRecipient = null;
	}

	/** Construct a Rhizome MeshMS manifest from an Android Bundle containing various manifest fields.
	 *
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	protected RhizomeManifest_MeshMS(Bundle b, byte[] signatureBlock) throws RhizomeManifestParseException {
		super(b, signatureBlock);
		mSender = parseSID("sender", b.getString("sender"));
		mRecipient = parseSID("recipient", b.getString("recipient"));
	}

	/** Return the service field.
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	@Override
	public String getService() {
		return RhizomeManifest_MeshMS.SERVICE;
	}

	@Override
	protected void makeBundle() {
		super.makeBundle();
		if (mSender != null)
			mBundle.putString("sender", mSender.toHex().toUpperCase());
		if (mRecipient != null)
			mBundle.putString("recipient", mRecipient.toHex().toUpperCase());
	}

	/** Return the 'sender' field (SID).
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public SubscriberId getSender() throws MissingField {
		missingIfNull("sender", mSender);
		return mSender;
	}

	/** Set the 'sender' field to null (missing) or the SID of a sender.
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public void setSender(SubscriberId sid) {
		mSender = sid;
	}

	/** Unset the 'sender' field.
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public void unsetSender() {
		mSender = null;
	}

	/** Return the 'recipient' field (SID).
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public SubscriberId getRecipient() throws MissingField {
		missingIfNull("recipient", mRecipient);
		return mRecipient;
	}

	/** Set the 'recipient' field to null (missing) or the SID of a recipient.
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public void setRecipient(SubscriberId sid) {
		mRecipient = sid;
	}

	/** Unset the 'recipient' field.
	 * @author Andrew Bettison <andrew@servalproject.com>
	 */
	public void unsetRecipient() {
		mRecipient = null;
	}

	@Override
	public String getDisplayName() {
		if (mSender != null && mRecipient != null)
			return mSender.abbreviation() + " - " + mRecipient.abbreviation()
					+ " - " + (mVersion == null ? null : mVersion);
		return super.getDisplayName();
	}

}
